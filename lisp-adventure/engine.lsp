(defun current-room-desc ()
  (thing-desc (cdr (assoc *r* *things*))))

(defun find-connection (dir room)
  (loop for (d r1 r2) in *go*
        when (and (eq d dir) (eq r1 room))
          return r2))

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

