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

    @Override
    public int hashCode() {
        return name.hashCode() + licence.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof User)) {
            return false;
        }
        return ((User) other).licence.equals(this.licence);
    }
}