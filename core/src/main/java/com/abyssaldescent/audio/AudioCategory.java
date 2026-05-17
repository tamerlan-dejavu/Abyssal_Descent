package com.abyssaldescent.audio;


public enum AudioCategory {
    MASTER,   // общая громкость (множитель для всех)
    MUSIC,    // фоновая музыка по ярусам
    SFX,      // звуковые эффекты (удары, подбор, двери)
    UI,       // звуки интерфейса (клики, hover)
    AMBIENT   // фоновые звуки уровня (капли воды, лава и т.д.)
}