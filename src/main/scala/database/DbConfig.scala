package database

object DbConfig {
//todo: Polecam pobawi się PureConfigiem i wczytywa te wartości z configu zewnętrznego.
  // https://www.baeldung.com/scala/pureconfig-load-config-files
//todo: Jest to znacznie bardziej elastyczne i ∂lugotrwałe podejście.

  val dbDriverName = "org.postgresql.Driver"
  val dbUrl        = "jdbc:postgresql://ec2-3-219-229-143.compute-1.amazonaws.com:5432/d182sekgnpojsa"
  val dbUser       = "juipfucijgyxox"
  val dbPwd        = "f0f48187ba25433138fb436a441ab7d5a3aad0611cd6c44efe66ec0d95b73706"
}
