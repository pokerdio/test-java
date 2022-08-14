
(defun del-from-down-lst (lst sym)
  "destructively deletes an item from a list except from the first element"
  (when (cdr lst)
    (if (eq sym (cadr lst))
        (setf (cdr lst) (cddr lst))
        (del-from-down-lst (cdr lst) sym))))

(defmacro returning (varname init-form &body body)
  `(let ((,varname ,init-form))
     (progn 
       ,@body
       ,varname)))

(defmacro iflet (varname condition-form &body body)
  `(let ((,varname ,condition-form))
     (if ,varname ,@body)))

(defmacro p (&rest args)
  `(format t (cat ,@args)))





(defun sym-to-low-str (sym)
  (if (symbolp sym)
      (string-downcase (string sym))
      sym))


(defun cat (&rest args)
  (format nil "~{~a~^~}" (mapcar #' sym-to-low-str args)))
