import os
import time
from gtts import gTTS

# Mapping Android resource key -> Hindi/Devanagari phonetic rendering for clear TTS output
letters = {
    "ol_a": "ऑ",
    "ol_at": "अत",
    "ol_ag": "अग",
    "ol_ang": "अंग",
    "ol_al": "अल",
    "ol_ak": "अक",
    "ol_ia": "ला",
    "ol_ih": "इह",
    "ol_is": "इस",
    "ol_ing": "इंग",
    "ol_ir": "इर",
    "ol_ilu": "लु",
    "ol_u": "उ",
    "ol_ep": "एप",
    "ol_edd": "एड",
    "ol_en": "एन",
    "ol_el": "एल",
    "ol_aw": "औ",
    "johar": "जोहार",
    "puthi": "पुथि",
    "mid": "मिद",
    "bar": "बार",
    "peya": "पेया"
}

output_dir = "raw_letters_clean"
os.makedirs(output_dir, exist_ok=True)

print("Generating clean TTS audio files for Android res/raw...")

for key, text in letters.items():
    output_path = os.path.join(output_dir, f"{key}.mp3")
    try:
        # Synthesize audio with Devanagari phonetics
        tts = gTTS(text=text, lang='hi', slow=False)
        tts.save(output_path)
        print(f"Generated: {key}.mp3 -> '{text}'")
        time.sleep(0.5)  # Pause to avoid rate limits
    except Exception as e:
        print(f"Error generating {key}: {e}")

print("\nSuccess! All clean MP3 files are saved inside 'raw_letters_clean'.")