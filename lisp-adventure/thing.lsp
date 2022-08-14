(defclass thing ()
  ((name :initarg :name
         :initform (error "thing must has name")
         :accessor thing-name)
   (desc :initarg :description
         :initform nil)
   (traits :initarg :traits
           :initform nil
           :accessor thing-traits)
   (owner :initarg :owner
          :initform nil
          :accessor thing-owner)
   (contents :initarg :contents
             :initform nil
             :accessor thing-contents)))

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




(defun add-thing (thing)
  (let ((sym (thing-name thing)))
    (iflet it (assoc sym *things*)
      (setf (cdr it) thing)
      (setf *things* (cons (cons (thing-name thing) thing)
                           *things*)))))

(defun thing-has (thing-sym item)
  (member item (thing-contents (get-thing thing-sym))))

(defun add-to-thing (thing-sym item)
  (setf (thing-owner (get-thing item)) thing-sym)
  (when (not (thing-has thing-sym item))
    (let ((thing (get-thing thing-sym)))
      (setf (thing-contents thing)
            (cons item (thing-contents thing))))))



(defun make-thing (sym traits desc &key (contents nil) (owner nil))
  (returning ret (make-instance 'thing
                                :name sym
                                :traits traits
                                :description desc
                                :owner owner
                                :contents contents)
    (add-thing ret)
    (when owner
      (add-to-thing owner sym))
    (when contents
      (dolist (x contents)
        (add-to-thing sym x)))))




