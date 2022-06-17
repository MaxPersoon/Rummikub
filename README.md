# Rummikub

This repository hosts a Thesis which was prepared in partial fulfilment of the requirements for the Degree of Bachelor of Science in Data Science 
and Artificial Intelligence, Maastricht University.

In this Thesis, the multi-player game 'Rummikub' has been fully implemented in Java, along with three optimisation techniques:
1. Integer linear programming
2. Alpha-beta pruning
3. Heuristic-greedy algorithm

And the following four objective functions:
1. Total tile count
2. Total tile count with set change minimisation
3. Total tile value
4. Total tile value with set change minimisation

For an explanation of these optimisation techniques and objective functions, we refer to this Thesis's accompanying report.

## How to run the program?
In order to run the program, one must first import the following libraries:
1. JavaFX from https://openjfx.io/
2. Google's OR-Tools from https://developers.google.com/optimization/install
3. Java Native Access (JNA) from https://github.com/java-native-access/jna

There is no GUI which allows for a configuration of the game. Instead, one must go to src/Main/Main.java and edit the lines numbered 25 and 29. On line number
25, one can set the optimisation technique to be used per player, whereas on line number 29, one can set the objective function to be used per player. Note that
the same index in both arrays points to the same player, e.g. playerTypes[0] and objectiveFunctions[0] denote the optimisation technique and objective function
to be used by player 1, respectively. Some technicalities:
- playerTypes and objectiveFunctions must be of equal size
- There is a minimum of two and a maximum of four players
- The visualisation has been designed for a 1920x1080 resolution
- Any combination of an optimisation technique and an objective function works

The game can be launched by running the main() method in the src/Main/Main.java file. The visualisation is enabled by default. For every move made in the game, the terminal will display detailed information.

## The experiment environment
src/Experiments houses the experiment environment, which is responsible for gathering data by means of running experiments and processing the acquired data.
Data that is directly acquired from the experiments may be found in the src/Experiments/rawData directory, whereas processed data is situated in the
src/Experiments/processedData directory.

## Usage and modification
The author of this project gives the public the permission to use and modify the code in this repository on the condition that the author is credited.
