package at.ac.tuwien.otl.ss18.pwlk.constructionHeuristic;

import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.SolutionInstance;

public interface IConstructSolution {
    SolutionInstance constructSolution(ProblemInstance problemInstance);
}
