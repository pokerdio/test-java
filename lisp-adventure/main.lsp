(eval-when (:compile-toplevel :load-toplevel :execute)
  (load (compile-file "util.lsp"))
  (load (compile-file "globals.lsp"))
  (load (compile-file "thing.lsp"))
  (load (compile-file "engine.lsp")))

(setf *pc* (make-thing 'pc '(player) "Just plain old you."))

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

(make-thing 'key '(pickable listable) "A small key."
            :owner (let ((lst '(house-ne house-se house-nw house-sw house-n house-e house-w)))
                     (nth (random (length lst)) lst)))

(make-thing 'hammer '(pickable listable) "A standard claw hammer."
            :owner 'pc)

(make-thing 'rug '(listable) "The floor is covered by a black woolen rug painted with bright red and yellow geometric patterns."
            :owner 'house)

                                        ;------------------------------

(defun go-room (room msg)
  (p msg)
  (setf *r* room)
  (p (thing-desc *r*)))

(match-com ()
  (p "Huh?"))

(match-com (look)
  (p (thing-desc *r*)))

(match-coms ((go down :in house)
             (down :in house))
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

(match-com (go)
  (p "Go where?"))

;; (match-coms ((look (x rug) (:dasein rug))
;;              (look (x key) (:dasein key)))
;;   (p (thing-desc x)))

(match-com (eq x x)
  (p "They are equal."))

(match-com (eq x y)
  (p "They are not equal."))

(match-com (take x :dasein x pickable)
  (del-thing x *r*)
  (add-to-thing x 'pc)
  (p "You take the " (sym-to-low-str x) "."))

(match-com (take x)
  (p "You can't take that."))


(match-com (drop x :having x)
  (del-thing x *pc*)
  (add-to-thing x *r*)
  (p "You drop the " (sym-to-low-str x) "."))

(match-com (drop x)
  (p "You don't have that."))


(match-coms ((inventory)
             (look inventory))
  (p "Your stuff is " (format nil "~{~A, ~}"
                              (mapcar #'sym-to-low-str
                                      (butlast (thing-contents *pc*))))
     (sym-to-low-str (car (last (thing-contents *pc*)))) "."))


(match-com (look x :all-things x)
  (p (thing-desc x)))

(match-com ((push move turn flip) rug :in house)
  (p "You push the rug, revealing a small trapdoor. ")
  (add-trait *r* 'trapdoor-revealed))

(match-com ((open close) trapdoor :room-trait trapdoor-revealed)
  (p "To take advantage of the trapdoor, just \"go down\"."))

(match-com (look x)
  (p "Can't see that here."))

(trail house-s e house-se n house-e n house-ne w house-n w house-nw
       s house-w s house-sw e house-s n house)

(trail-1-way cellar up house)



