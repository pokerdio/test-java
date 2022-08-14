(eval-when (:compile-toplevel :load-toplevel :execute)
  (require "asdf")
  (load (compile-file "util.lsp"))
  (load (compile-file "globals.lsp"))
  (load (compile-file "thing.lsp"))
  (load (compile-file "engine.lsp")))

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


(make-thing 'key '(pickable listable) "A small key."
            :owner (let ((lst '(house-ne house-se house-nw house-sw house-n)))
                     (nth (random (length lst)) lst)))
