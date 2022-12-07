package com.gogitek.btl.processing;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.gogitek.btl.TeamEnum;
import com.gogitek.btl.controller.ResultApi;
import com.gogitek.btl.ga.*;
import com.gogitek.btl.model.Season;
import com.gogitek.btl.model.Match;
import com.gogitek.btl.model.MatchSchedule;
import com.gogitek.btl.model.Team;

import java.util.ArrayList;
import java.util.List;

public class BusinessResolveService extends AbstractActor {

    private Population currentPopulation;

    private final int populationSize;
    private final GeneticAlgorithm geneticAlgorithm;
    private int currentGeneration = 1;

    private final double mutationRate;
    private final double crossoverRate;
    private final int elitismCount;
    private Season season;

    private static List<ResultApi> resultApi = new ArrayList<>();

    public BusinessResolveService(int populationSize, double mutationRate, double crossoverRate, int elitismCount) {
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.elitismCount = elitismCount;
        this.geneticAlgorithm = new GeneticAlgorithm(this.populationSize, mutationRate, crossoverRate, elitismCount);
    }

    static public Props props() {
        return Props.create(BusinessResolveService.class, () -> new BusinessResolveService(100, 0.01, 0.95, 0));
    }

    static public class Init {

        public Init() {
        }
    }

    static public class Result {

        public final Genotype genotype;

        public Result(Genotype genotype) {
            this.genotype = genotype;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Init.class, init -> executeInitLogic())
                .match(Result.class, result -> resultApi = evaluateResult(result.genotype))
                .build();
    }

    public static List<ResultApi> getDataForApi() {
        ActorSystem actorSystem = ActorSystem.create("btl");
        ActorRef masterActor = actorSystem.actorOf(BusinessResolveService.props());
        masterActor.tell(new BusinessResolveService.Init(), null);
        return resultApi;
    }

    public List<ResultApi> evaluateResult(Genotype genotype) {
        this.currentPopulation.addGenotype(genotype);
        List<ResultApi> res = new ArrayList<>();
        if (this.currentPopulation.populationSize() == this.populationSize) {
            //recalculateFitness();
            this.currentPopulation = this.geneticAlgorithm.mutatePopulation(this.currentPopulation, this.season);
            recalculateFitness(this.currentPopulation);
            if (!this.geneticAlgorithm.isTerminationConditionMet(this.currentPopulation)) {
                System.out.println("Generation: " + this.currentGeneration + " fittest " + this.currentPopulation.getFittest().getFitness());
                Population previousGeneration = new Population(this.currentPopulation);
                this.currentPopulation.getGenotypes().clear();
                for (int i = 0; i < populationSize; i++) {
                    GeneticAlgorithmProcessing.RegenerateGenotype regenerateGenotype = new GeneticAlgorithmProcessing.RegenerateGenotype(i, crossoverRate, previousGeneration, mutationRate, elitismCount, season);
                    ActorRef workerActor = getContext().actorOf(GeneticAlgorithmProcessing.props(), "Generation-" + this.currentGeneration + "-Child-" + i);
                    workerActor.tell(regenerateGenotype, getSelf());

                }
                this.currentGeneration++;


            } else {
                res = printSolution();
                context().stop(getSelf());
                context().system().terminate();

            }
        }
        return res;

    }


    public void recalculateFitness(Population population) {
        population.calculateFitness();
        population.sortBasedOnFitness();

    }


    public void executeInitLogic() {
        //Create Initial Population
        this.season = initSeason();
        Population population = this.geneticAlgorithm.initPopulation(season);
        this.currentPopulation = population;
        for (Genotype genotype : population.getGenotypes()) {
            GeneticAlgorithmProcessing.CalculateFitness calculateFitness = new GeneticAlgorithmProcessing.CalculateFitness(genotype, season.getTeams());
            ActorRef workerActor = getContext()
                    .actorOf
                            (GeneticAlgorithmProcessing.props(), "Generation-1" + "-Child-" + population.getGenotypes().indexOf(genotype));
            workerActor.tell(calculateFitness, getSelf());
        }
        this.currentGeneration++;
        this.currentPopulation.getGenotypes().clear();

    }


    public List<ResultApi> printSolution() {
        System.out.println("Found solution in " + this.currentGeneration + " generations");
        System.out.println("Best solution fitness: " + this.currentPopulation.getFittest().getFitness());
        System.out.println("Best solution Genotype: " + this.currentPopulation.getFittest().toString());
        System.out.println();
        System.out.println("###############################");
        System.out.println("Tournament");
        System.out.println("###############################");
        System.out.println();
        List<ResultApi> res = new ArrayList<>();
        Genotype fittest = this.currentPopulation.getFittest();
        fittest.createPhenoType(season.getTeams());
        //season.createSeasonSchedule(fittest);
        List<MatchSchedule> seasonSchedule = fittest.getPhenotype().getMatchSchedules();
        for (MatchSchedule matchSchedule : seasonSchedule) {
            int matchDayNumber = seasonSchedule.indexOf(matchSchedule) + 1;
            System.out.println("MatchDay: " + matchDayNumber);
            for (Match match : matchSchedule.getMatches()) {
                Integer[] match1 = match.getMatch();
                int matchNumber = matchSchedule.getMatches().indexOf(match) + 1;
                Team teamA = season.getTeamBasedOnId(match1[0]);
                Team teamB = season.getTeamBasedOnId(match1[1]);
                ResultApi resApi = new ResultApi();
                resApi.setTeam1(teamA.getTeamName());
                resApi.setTeam2(teamB.getTeamName());
                System.out.println("Match " + matchNumber + ": " + teamA.getTeamName() + " (H)" + " Vs " + teamB.getTeamName() + " (A)");
                res.add(resApi);
            }
            System.out.println();
        }
        return res;
    }

    public static Season initSeason() {
        Season season = new Season();
        for(TeamEnum e : TeamEnum.values()){
            season.addTeam(e.ordinal()+1, e.name());
        }
        return season;

    }

}
