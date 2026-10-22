package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.AuthResponse;

public record IssuedSession(AuthResponse response, String refreshToken) {
}
