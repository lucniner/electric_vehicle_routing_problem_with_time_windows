package at.ac.tuwien.otl.ss18.pwlk;

import at.ac.tuwien.otl.ss18.pwlk.dto.ProblemInstance;
import at.ac.tuwien.otl.ss18.pwlk.scenario.ProblemReader;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {
    final ProblemReader problemReader = new ProblemReader("instances/r102C10.txt");
    final ProblemInstance instance = problemReader.retrieveProblemInstance();

  }
}
