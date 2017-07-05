(ns drill.training.speech
  (:require [clojure.core.async :refer [<! chan close!]]
            [drill.app-state :refer [*profile]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(declare cancel!)

(set! *warn-on-infer* true)

(def synth js/speechSynthesis)

(defn get-voice [lang]
  (let [^js/Array voices (.getVoices synth)
        voice-name (case lang
                     "ru" "Google русский"
                     "Google UK English Male")]
    (.find voices #(= (.-name ^js/SpeechSynthesisVoice %) voice-name))))

(defn activate! [*ref text]
  (go (cancel! *ref)
      (let [lang (:speakLanguage @*profile)
            utterance (js/SpeechSynthesisUtterance. text)
            ch (chan 1)]
        (set! (.-voice utterance) (get-voice lang))
        (.speak synth utterance)
        (reset! *ref true)
        (set! (.-onend utterance) #(close! ch))
        (<! ch)
        (reset! *ref false))))

(defn cancel! [*ref]
  (.cancel synth)
  (reset! *ref false))
