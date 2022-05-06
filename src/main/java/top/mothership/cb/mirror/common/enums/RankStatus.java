package top.mothership.cb.mirror.common.enums;

import java.util.Arrays;
import java.util.Objects;

public enum RankStatus {
    LOVED(4),
    QUALIFIED(3),
    APPROVED(2),
    RANKED(1),
    PENDING(0),
    WIP(-1),
    GRAVEYARD(-2),
    ;

    public boolean isUnranked() {
        return getCode() <= 0;
    }

    public boolean isRankedOrLoved() {
        return getCode() == 1 || getCode() == 4;
    }

    public static RankStatus toStatus(int code) {
        return Arrays.stream(RankStatus.values())
                .filter(s -> Objects.equals(s.code, code))
                .findFirst().orElse(null);
    }


    public int code;

    public int getCode() {
        return code;
    }

    RankStatus(int code) {
        this.code = code;
    }
}
