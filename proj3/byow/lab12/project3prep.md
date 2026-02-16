# Project 3 Prep

**For tessellating hexagons, one of the hardest parts is figuring out where to place each hexagon/how to easily place hexagons on screen in an algorithmic way.
After looking at your own implementation, consider the implementation provided near the end of the lab.
How did your implementation differ from the given one? What lessons can be learned from it?**

Answer:
Our calculation shows that the starting point of a hexagon is different. 
He treats the hexagon as a rectangle and then takes the top left corner of the rectangle 
to calculate the blank cells and the cells that need to be filled, 
while I directly calculate the top and left cells of the rectangle
At first, I was thinking about how to represent the position, whether to create a tuple 
or variable to represent it. 
Later, his answer was to set a class to represent the position, which I think is very good
-----

**Can you think of an analogy between the process of tessellating hexagons and randomly generating a world using rooms and hallways?
What is the hexagon and what is the tesselation on the Project 3 side?**

Answer:
The world is a mosaic of corridors and rooms, but no longer a single graphic
-----
**If you were to start working on world generation, what kind of method would you think of writing first? 
Think back to the lab and the process used to eventually get to tessellating hexagons.**

Answer:
I will first write down how to move the coordinates, 
and then consider the connection between the room and the corridor
-----
**What distinguishes a hallway from a room? How are they similar?**

Answer:
The room only has one exit or one door, while there are many corridors, 
all of which are generated in different shapes. 
The corridors are long and narrow, and the room length and width are similar
