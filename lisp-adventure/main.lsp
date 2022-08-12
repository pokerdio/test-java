(require "asdf")

(defparameter *t* '())
(defparameter *r* 'house-s)
(defparameter *f* '())
(defparameter *ignore-tokens*
  '("" "the" "an" "a"))

(defparameter *translate*
  '((n . north) (s . south) (e . east) (w . west)
    (u . up) (d . down)
    (q . quit)
    (l . look)
    (x . look)
    (examine . look)
    (see . look)
    (q . quit)))

(defparameter *go* '())

(defparameter *command-handled* nil)

(defparameter *things* nil)


(defclass thing ()
  ((name :initarg :name
         :initform (error "thing must has name")
         :accessor thing-name)
   (desc :initarg :description
         :initform nil)
   (traits :initarg :traits
           :initform nil
           :accessor thing-traits)
   (has :initarg :contents
        :initform nil
        :accessor thing-has)))


(defun get-thing (thing-sym)
  (cdr (assoc thing-sym *things*)))

(defun has-trait (thing-sym trait)
  (let ((thing (get-thing thing-sym)))
    (not (not (member trait (thing-traits thing))))))

(defun add-trait (thing-sym trait)
  (let ((thing (get-thing thing-sym)))
    (with-accessors ((traits thing-traits)) thing
      (when (not (member trait traits))
        (setf traits (cons trait traits))))))

(defun del-from-down-lst (lst sym)
  "destructively deletes an item from a list except from the first element"
  (when (cdr lst)
    (if (eq sym (cadr lst))
        (setf (cdr lst) (cddr lst))
        (del-from-down-lst (cdr lst) sym))))

(defun del-trait (thing-sym trait)
  (let ((thing (get-thing thing-sym)))
    (with-accessors ((traits thing-traits)) thing
      (when (has-trait trait thing)
        (if (eq (car traits) trait)
            (setf traits (cdr traits))
            (del-from-down-lst traits trait))))))

(defmethod thing-desc ((x thing))
  (with-slots (name desc)
      x
    (if desc desc
        (format nil "This is a ~A." (sym-to-low-str name)))))

(defmethod print-object ((x thing) out)
  (format out "thing:~A" (thing-name x)))

(defmethod detail-thing ((x thing))
  (format t "thing(~A|~A|~A)"
          (thing-name x)
          (thing-traits x)
          (thing-desc x)))

;; (remove-method #'detail-object
;;                (find-method #'detail-object '() (list (find-class 'thing) t)))

(defmacro returning (varname init-form &body body)
  `(let ((,varname ,init-form))
     (progn 
       ,@body
       ,varname)))

(defmacro iflet (varname condition-form &body body)
  `(let ((,varname ,condition-form))
     (if ,varname ,@body)))


(defun add-thing (thing)
  (let ((sym (thing-name thing)))
    (iflet it (assoc sym *things*)
      (setf (cdr it) thing)
      (setf *things* (cons (cons (thing-name thing) thing)
                           *things*)))))

(defun make-thing (sym traits desc &optional (contents nil))
  (returning ret (make-instance 'thing
                                :name sym
                                :traits traits
                                :description desc
                                :contents contents)
    (add-thing ret)))



(make-thing 'house-ne '(room) "You're in the garden of a small house.")
(make-thing 'house-s '(room) "You're in the garden of a small house. ~
The front door of the house lies to the north. ")
(make-thing 'house-e '(room) "You're in the garden of a small house.")
(make-thing 'house-n '(room) "You're in the garden of a small house.")
(make-thing 'house-w '(room) "You're in the garden of a small house.")
(make-thing 'house-se '(room) "You're in the garden of a small house.")
(make-thing 'house-sw '(room) "You're in the garden of a small house.")
(make-thing 'house-nw '(room) "You're in the garden of a small house.")
(make-thing 'house '(room) "You're inside the only room of the house. It looks much larger than you had expected. The floor is covered by a thick rug.")

(make-thing 'cellar '(room) "You're inside a small, dark cellar with brick walls lined by empty shelves. Stairs go upward. ")

(defmacro p (&rest args)
  `(format t (cat ,@args)))

(defun current-room-desc ()
  (thing-desc (cdr (assoc *r* *things*))))


(defun reverse-dir (dir)
  (cond ((eq dir 'east) 'west)
        ((eq dir 'south) 'north)
        ((eq dir 'north) 'south)
        ((eq dir 'west) 'east)
        ((eq dir 'up) 'down)
        ((eq dir 'down) 'up)))

(defun find-connection (dir room)
  (loop for (d r1 r2) in *go*
                                        ;do (print (cat d r1 r2))
        when (and (eq d dir) (eq r1 room))
          return r2))

(defun trans (sym)
  (if (assoc sym *translate*)
      (cdr (assoc sym *translate*))
      sym))

(defun connect-1-way (place1 dir place2)
  (setf dir (trans dir))
  (setf *go* (cons (list dir place1 place2) *go*)))

(defun connect (place1 dir place2)
  (setf dir (trans dir))
  (connect-1-way place1 dir place2)
  (connect-1-way place2 (reverse-dir dir) place1))

(defun expand-triplets-by-two (lst)
  (when (cdr lst)
    (cons (list (car lst) (cadr lst) (caddr lst))
          (expand-triplets-by-two (cddr lst)))))

(defun quote-every (lst)
  (when lst
    (cons (list 'quote (car lst))
          (quote-every (cdr lst)))))

(defmacro trail (&rest syms)
  (cons 'progn
        (mapcar #'(lambda (x) (cons 'connect (quote-every x)))
                (expand-triplets-by-two syms))))

(defmacro trail-1-way (&rest syms)
  (cons 'progn
        (mapcar #'(lambda (x) (cons 'connect-1-way (quote-every x)))
                (expand-triplets-by-two syms))))


(defun var? (sym)
  (member sym '(x y z a b c d e f g h i j k l m n o p q r s u v w)))

(defun tokenize-string (s)
  (setf s (uiop:split-string s :separator '(#\  #\Tab #\, #\! #\? #\.)))
  (setf s (remove-if #'(lambda (x) (member (string-downcase x)
                                           *ignore-tokens* :test #'string=))
                     s))
  (setf s (mapcar #'(lambda (x) (intern (string-upcase x))) s))
  (setf s (mapcar #'(lambda (x) (let ((a (assoc x *translate*)))
                                  (if a (cdr a) x)))
                  s))
  s)

(defmacro while (test &body decls/tags/forms)
  `(do () ((not ,test) (values))
     ,@decls/tags/forms))


(defun vars-in-pat (pat)
  "returns the list of variables in a pattern, sorted alphabetically"
  (sort (loop for x in pat
              when (var? x)
                collect x)
        #'string-lessp))

(defun neighbors-different (pat)
  "checks if all the elements in pat are different from adjacent elements - \
intended use with sorted lists"
  (cond ((not pat) t)
        ((not (cdr pat)) t)
        ((not (equal (car pat) (cadr pat)))
         (neighbors-different (cdr pat)))
        (t nil)))


(defun all-same (pats)
  "checks if all elements are equal"
  (if (or (not pats) (not (cdr pats))) t
      (every #'(lambda (x) (equal x (car pats)))
             (cdr pats))))

(defun valid-match-list (pats)
  "makes sure a list of match patterns has the same variable sets, \
 with no repeating variables"
  (assert pats) ; no empties plox
  (let ((var (mapcar #'vars-in-pat pats)))
    (and (neighbors-different (car var))
         (all-same var))))


(defun game-read-line ()
  (format t "> ")
  (read-line))

(defun sym-to-low-str (sym)
  (if (symbolp sym)
      (string-downcase (string sym))
      sym))


(defun cat (&rest args)
  (format nil "~{~a~^~}" (mapcar #' sym-to-low-str args)))

(defun build-match-var-wrap (input-sym var body)
  `(when ,input-sym
     (let ((,var (car ,input-sym))
           (,input-sym (cdr ,input-sym)))
       ,@body)))

(defun build-match-const-wrap (input-sym c body)
  `(when (and ,input-sym (eq (car ,input-sym) ',c))
     (let ((,input-sym (cdr ,input-sym)))
       ,@body)))

(defun build-match-var-opt-wrap (input-sym v-form body)
  (let ((var (car v-form))
        (opt (cdr v-form)))
    `(when (and ,input-sym (member (car ,input-sym) ',opt))
       (let ((,var (car ,input-sym))
             (,input-sym (cdr ,input-sym)))
         ,@body))))

(defun build-match-var-const-wrap (input-sym const-list body)
  `(when (and ,input-sym (member (car ,input-sym) ',const-list))
     (let ((,input-sym (cdr ,input-sym)))
       ,@body)))

(defun build-match-in-room-wrap (room-lst body)
  `(when (member *r* ',room-lst)
     ,@body))

(defun build-match-lambda-body (input-sym pat body)
  (if pat
      (let ((var1 (car pat))
            (rest-body (list (build-match-lambda-body input-sym (cdr pat) body))))
        (let ((ret
                (cond ((var? var1)
                       (build-match-var-wrap input-sym var1 rest-body))
                      ((and var1 (listp var1) (var? (car var1)))
                       (build-match-var-opt-wrap input-sym var1 rest-body))
                      ((and var1 (listp var1) (eq :in (car var1)))
                       (build-match-in-room-wrap (cdr var1) rest-body))
                      ((and var1 (listp var1))
                       (build-match-var-const-wrap input-sym var1 rest-body))
                      (t (build-match-const-wrap input-sym var1 rest-body)))))
          ret))
      `(when (not ,input-sym) ,@body)))


(defmacro match-com (pat &body body)
  (let ((com-sym (gensym))
        (body (append body '((setf *command-handled* t)))))
    `(setf *f*
           (append *f*
                   (list #'(lambda (,com-sym)
                             ,(build-match-lambda-body com-sym pat body)))))))


(defmacro match-coms (pats &body body)
  `(progn
     ,@(loop for pat in pats
             collect `(match-com ,pat ,@body))))

(defun process-commands (com)
  (let ((*command-handled* nil))
    (do ((f *f* (cdr f)))
        ((or (not f) *command-handled*) nil)
      (funcall (car f) com))))

(defun game-loop ()
  (do ((com (tokenize-string (game-read-line))
            (tokenize-string (game-read-line))))
      ((equal com '(quit)) t)
    (process-commands com)
    (terpri)))

            ;------------------------------

(defun go-room (room msg)
  (p msg)
  (setf *r* room)
  (p (current-room-desc)))

(match-com (look)
  (p (current-room-desc)))

(match-coms (((:in house) go down)
             ((:in house) down))
  (if (has-trait *r* 'trapdoor-revealed)
      (progn
        (go-room 'cellar "You climb down the steep stairs. "))
      (p "You can't go there.")))

(match-coms ((go (x north west east south down up))
             ((x north west east south down up)))
  (let ((new-room (find-connection x *r*)))
    (if new-room
        (progn
          (go-room new-room (cat "You go " x ".~%")))
        (p "You can't go there."))))

(match-com (look rug (:in house))
  (p "It is a black woolen rug painted with bright red geometric patterns."))

(match-com ((:in house) (push move turn flip) rug)
  (p "You push the rug, revealing a small trapdoor. ")
  (add-trait *r* 'trapdoor-revealed))

(match-com (look x)
  (p "Nothing interesting about a " x "."))

(trail house-s e house-se n house-e n house-ne w house-n w house-nw
       s house-w s house-sw e house-s n house)

(trail-1-way cellar up house)
