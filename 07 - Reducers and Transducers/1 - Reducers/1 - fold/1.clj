(fold
  ([reducef coll])
  ([combinef reducef coll] )
  ([n combinef reducef coll]))