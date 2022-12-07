package com.gogitek.btl.model;

import com.gogitek.btl.TeamEnum;
import com.gogitek.btl.ga.Genotype;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Season {

    private final HashMap<Integer, Team> teams;
    private final List<MatchSchedule> seasonSchedule;


    public Season() {
        this.teams = new HashMap<>();
        this.seasonSchedule = new ArrayList<>();
    }

    public Season(Season cloneable) {
        this.teams = cloneable.teams;
        this.seasonSchedule = new ArrayList<>();
    }


    public void addTeam(int teamId, String teamName) {
        this.teams.put(teamId, new Team(teamId, teamName));
    }

    public void createSeasonSchedule(Genotype genotype) {
        int numberOfTeams = teams.size();
        int matchDays = (numberOfTeams - 1) * 2;
        int[] chromosome = genotype.getChromosome();
        int chromsoPos = 0;
        int seasonSchedulePos = 0;
        for (int i = 0; i < matchDays; i++) {
            MatchSchedule matchSchedule = new MatchSchedule();
            for (int j = 0; j < numberOfTeams / 2; j++) {
                int teamA = chromsoPos++;
                int teamB = chromsoPos++;
                Match match = new Match(chromosome[teamA], chromosome[teamB]);
                if(checkIsDerby(teamA, teamB)){
                    match.setDerby(true);
                }
                matchSchedule.add(match);
            }
            this.seasonSchedule.add(matchSchedule);

        }
        //System.out.println(this.seasonSchedule);
    }

    private boolean checkIsDerby(int teamA, int teamB){
        Team teamAName = teams.get(teamA);
        Team teamBName = teams.get(teamB);

        if(teamAName.getTeamName().equals(TeamEnum.ARSENAL.name()) && teamBName.getTeamName().equals(TeamEnum.CHELSEA.name())){
            return true;
        }

        if(teamAName.getTeamName().equals(TeamEnum.MAN_UTD.name()) && teamBName.getTeamName().equals(TeamEnum.MANCHESTER_CITY.name())){
            return true;
        }

        return false;
    }


    public int getNumberOfTeams() {
        return this.teams.size();
    }


    public Team getRandomTeam() {
        Object[] teamArrays = this.teams.values().toArray();
        int randomId = (int) (teamArrays.length * Math.random());
        Team team = (Team) teamArrays[randomId];
        return team;


    }


    public int calculateClashes() {
        int clashes;
        int numberOfTimeSameMatchBeingPlayed;
        int teamsPlayingMultipleMatchesSameDay = 0;
        int teamsPlayingAgainstEachOther = 0;
        int notSoftConstraints = 0;
        //Get all matches for a particular schedule
        List<Match> allMatches = seasonSchedule
                .stream()
                .flatMap(x -> x.getMatches().stream())
                .collect(Collectors.toList());

        // Calculate number of times same match being played
        Map<Match, Long> matchesMap = allMatches.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        numberOfTimeSameMatchBeingPlayed = matchesMap.values().stream()
                .filter(x -> x.intValue() > 1)
                .mapToInt(Long::intValue)
                .sum();
        if (numberOfTimeSameMatchBeingPlayed != 0) {
            numberOfTimeSameMatchBeingPlayed = numberOfTimeSameMatchBeingPlayed / 2;
        }
        for (MatchSchedule matchSchedule : seasonSchedule) {
            Integer[] matchDayChromosome = matchSchedule
                    .getMatches()
                    .stream()
                    .flatMap(x -> Stream.of(x.getMatch()))
                    .toArray(Integer[]::new);
            Map<Integer, Long> teamPlayingMultipleTimesSameDayMap = Arrays
                    .stream(matchDayChromosome)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            int sum = teamPlayingMultipleTimesSameDayMap.values().stream()
                    .filter(x -> x.intValue() > 1)
                    .mapToInt(Long::intValue)
                    .sum();
            if (sum != 0) {
                sum = sum / 2;
            }
            teamsPlayingMultipleMatchesSameDay = teamsPlayingMultipleMatchesSameDay + sum;
//            if (teamsPlayingMultipleMatchesSameDay != 0) {
//                teamsPlayingMultipleMatchesSameDay = teamsPlayingMultipleMatchesSameDay + teamsPlayingMultipleMatchesSameDay / 2;
//
//
//            }
        }
//        long teamsPlayingAgainstEachOtherLong = allMatches.stream().filter(x -> x.getMatch()[0] == x.getMatch()[1])
//                .count();
//        teamsPlayingAgainstEachOther = (int) teamsPlayingAgainstEachOtherLong;
        clashes = numberOfTimeSameMatchBeingPlayed + teamsPlayingMultipleMatchesSameDay + teamsPlayingAgainstEachOther;
        return clashes;


    }

    public List<MatchSchedule> getSeasonSchedule() {
        return seasonSchedule;
    }


    public Team getTeamBasedOnId(int teamId) {
        return this.teams.get(teamId);
    }

    public HashMap<Integer, Team> getTeams() {
        return teams;
    }
}
