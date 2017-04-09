## Van Emde Boas tree

There are java and c++ versions of the tree. c++ version is not so beautiful (using raw pointers etc) but it is fast. Four times faster that std::set with w = 17 to be exact. Java version is actually slower than a TreeSet even though java and c++ versions are identical in terms of the algorithm. Probably java handles memory too slowly.

Testing java version: run `sh test.sh veb` in root directory.

There is an interactive runner, quite useful for debugging.