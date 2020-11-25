# GBZ80AsmCondenser

The goal of this project is to make gbz80 asm easier to follow, by following register values through calls and jumps automatically. 

The program will take a method name. Then the progam will fill in calls and jumps with the code called or jumped to. Then the program will go up the call stack to find unknown variables on the right hand side of instructions, or in brackets on the left hand side, using user input to choose the call trace.

This will be done with the following steps:
The first step will write code that is called or jumped to inline.
The second step will refactor the asm to a format used in the following steps. 
The third step will replace unknowns like described above.
The fourth step will go through and delete assignments that are not needed to understand the code. 
  Example (after the second step):
  ```
  .routine
    a = 4
    [de] = 4
    a = 8
  .
  .
  .
  ```
  will become 
  ```
  .routine
    [de] = 4
    a = 8
  .
  .
  .
  ```
  after the fourth step.
