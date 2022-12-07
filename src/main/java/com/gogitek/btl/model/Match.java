package com.gogitek.btl.model;

import java.util.Arrays;

public class Match {

    private final Integer[] match;
    private Boolean isDerby;

    public Match(int homeTeam, int awayTeam) {
        this.match = new Integer[2];
        this.match[0] = homeTeam;
        this.match[1] = awayTeam;
        this.isDerby = false;
    }


    public Integer[] getMatch() {
        return match;
    }

    public Boolean getDerby() {
        return isDerby;
    }

    public void setDerby(boolean isDerby){
        this.isDerby = isDerby;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match1 = (Match) o;
        return Arrays.equals(getMatch(), match1.getMatch());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getMatch());
    }
}
