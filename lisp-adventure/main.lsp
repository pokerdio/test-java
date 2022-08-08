(require "asdf")

(defparameter *t* '())
(defparameter *r* 'house-s)
(defparameter *f* '())
(defparameter *ignore-tokens*
  '("" "the" "an" "a"))

(defparameter *translate*
  '((n . north)
	(s . south)
	(e . east)
	(w . west)
	(q . quit)
	(l . look)
	(x . look)
	(examine . look)
	(see . look)
	(q . quit)))


(defparameter *desc*
  '((house-ne . "You're in the garden of a small house.")
	(house-s . "You're in the garden of a small house. The front door of the house lies to the north. ")
	(house-e . "You're in the garden of a small house.")
	(house-n . "You're in the garden of a small house.")
	(house-w . "You're in the garden of a small house.")
	(house-se . "You're in the garden of a small house.")
	(house-sw . "You're in the garden of a small house.")
	(house-nw . "You're in the garden of a small house.")
	(house . "You're inside a house. On the inside it looks larger than you had expected. The floor is covered by a thick rug.")))

(defparameter *go* '())

(defparameter *command-handled* nil)

(defmacro p (&rest args)
  `(format t (cat ,@args)))

(defun current-room-desc ()
  (cdr (assoc *r* *desc*)))

(defun reverse-dir (dir)
  (cond ((eq dir 'east) 'west)
		((eq dir 'south) 'north)
		((eq dir 'north) 'south)
		((eq dir 'west) 'east)))

(defun find-connection (dir room)
  (loop for (d r1 r2) in *go*
		;do (print (cat d r1 r2))
		when (and (eq d dir) (eq r1 room))
		  return r2))

(defun trans (sym)
  (if (assoc sym *translate*)
	  (cdr (assoc sym *translate*))
	  sym))

(defun connect (place1 dir place2)
  (setf dir (trans dir))
  (setf *go* (cons (list dir place1 place2)  *go*))
  (setf *go* (cons (list (reverse-dir dir) place2 place1) *go*)))

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

(defstruct thing
  name def-name indef-name
  sym
  in-thing)

(defun new-thing (name &key sym def-name indef-name in-thing)
  (if (not name)
	  (setf name "thing"))
  (setf name (string-downcase (string name)))
  (if (not sym)
	  (setf sym (intern (string-upcase name))))
  (if (not def-name)
	  (setf def-name (cat "the" name)))
  (if (not indef-name)
	  (setf indef-name (cat "a" name)))
  (setf *t*
		(cons (cons sym (make-thing :name name
									:def-name def-name
									:indef-name indef-name
									:in-thing in-thing
									:sym sym))
			  *t*)))

(defun monkey (x)
  (format t "***~a***" x))

(format nil "it was the fuck fuck ~
it was the fuck fuck")

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

(defun build-match-in-room-wrap (input-sym room-lst body)
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
					   (build-match-in-room-wrap input-sym (cdr var1) rest-body))
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

(match-com (look)
  (p (current-room-desc)))

(match-coms ((go (x north west east south))
			 ((x north west east south)))
  (let ((new-room (find-connection x *r*)))
	(if new-room
		(progn
		  (setf *r* new-room)
		  (p "You go " x ".~%")
		  (p (current-room-desc)))
		(p "You can't go there."))))

(match-com (look rug (:in house))
  (p "It is a black woolen rug painted with bright red geometric patterns."))

(match-com ((:in house) (push move turn flip) rug)
  (p "You push the rug, revealing a small trapdoor. "))

(match-com (look x)
  (p "Nothing interesting about a " x "."))

(trail house-s e house-se n house-e n house-ne w house-n w house-nw
	   s house-w s house-sw e house-s n house)
