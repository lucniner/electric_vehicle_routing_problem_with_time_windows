package at.ac.tuwien.otl.ss18.pwlk;

import at.ac.tuwien.otl.ss18.pwlk.reader.ProblemReader;
import at.ac.tuwien.otl.ss18.pwlk.valueobjects.ProblemInstance;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {
    final ProblemReader problemReader = new ProblemReader("instances/r102C10.txt");
    final ProblemInstance instance = problemReader.retrieveProblemInstance();

  }
}
