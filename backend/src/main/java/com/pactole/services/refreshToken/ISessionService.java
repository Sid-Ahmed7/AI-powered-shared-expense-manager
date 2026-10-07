package com.pactole.services.refreshToken;

import com.pactole.dto.request.session.SessionRequest;
import com.pactole.dto.request.session.SessionViewRequest;

public interface ISessionService {
    public record IssuedSession(SessionRequest sessionRequest) {}
    public record SessionView(SessionViewRequest sessionViewRequest) {}
}
