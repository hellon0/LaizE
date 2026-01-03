# LaizE
Coding language I made that runs through Java. Doesn't work the same as a compiler, as that's beyond my abilities at the moment, but this was used as a project to become more proficient in Java. Very rudimentery with many issues.


##Syntax:


### Variables:
```
var value = 0;
value = value + 1;
```
When assigning a value to a variable, a variable type will be auto assigned, making you unable to (for example) change an integer to a string. A variable will be assigned to a boolean if it has a true or false value, or if it contains a comparator. 'And' and 'or' statements work the same as they do in java (ex. condition1 == condition2 || condition3 == condition4). Variable types include string, int, dec (decimal), and boolean. To reassign a variable value, enter the same statement not including 'var'. There's currently no shortcut to add, subtract, divide, or multiply numbers.

### Print:
`prt("Hello World"); `
Use '+' to insert a variable into the statement

### For Loops:
```
for (i < 3; +1) {
  
}
```
'i', '3', and '1' can be replaced with an already declared variable. If not using an undeclared variable, i will default to having a value of 0. The '<' can be replaced with the comparators: ==, !=, <=, >=, >, and <.

### If/Elsif/Else statements:
```
if (value == 1) {
  prt("value = 1");
} elsif (value == 2) {
  prt("value = 2");
} else {
  prt("value doesn't equal 1 or 2");
}
```
The only difference between these and the ones in Java are 'else if' being changed to 'elsif'. I'm very creative as you can see.

### While loops:
```
while (value != 2) {
 value = value + 1;
}
```
There's no difference between these and while loops in Java.

### Math:
```
var value = 2*(3+5)/2-4;
```
Math isn't any different to Java, other than lacking features like Modulus and Exponents. Math methods are also not included yet.


### Unincluded Features
Features I've yet to include are `break`, math shortcuts (`value++;`), variable methods.

Let me know if I forgot anything!
