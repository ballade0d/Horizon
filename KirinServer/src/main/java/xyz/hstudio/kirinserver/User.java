package xyz.hstudio.kirinserver;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@RequiredArgsConstructor
public class User {

    public final String name;
    public final String licence;
    public final String email;
    public final Set<String> ips;
    public final LocalDateTime expireTime;
}