# GBZ80AsmCondenser

The goal of this project is to make gbz80 asm easier to follow, by following register values through calls and jumps automatically.

The goal is a 4 pass program:
The first pass will refactor the asm to a format used in the second pass. 
The second pass will follow the values of registers and write their values in places where the registers are used. 
The third pass will go through a delete assignments that are not needed to understand the code. 
  Example (after a 2nd pass):
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
The fourth pass will go through and write the possible register values in routines without assignments to those registers by 
finding the callers and the value of the registers when they are called. For example, ```[de]```  will have de replaced by the values it contains from its callers.
