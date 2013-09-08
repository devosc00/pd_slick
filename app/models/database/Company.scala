package models

import java.util.Date

import play.api.Play.current

import play.api.db.slick.Config.driver.simple._

import slick.lifted.{Join, MappedTypeMapper}

/*object Company extends Table[(Long, String, Int)]("Company") {
	def id = column[Long]("ID", O.PrimaryKey)
	def name = column[String]("C_NAME")
	def orders = column[Int]("ORDERS")
	def * = id ~ name ~ orders
}*/

case class Company(id: Option[Long], name: String, orders: Int)

object Companies extends Table[Company]("COMPANY") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def orders = column[Int]("orders")
  def * = id.? ~ name ~ orders <> (Company.apply _, Company.unapply _)
  def autoInc = * returning id


 def options(implicit s:Session): Seq[(String, String)] = {
    val query = (for {
      company <- Companies
    } yield (company.id, company.name)
      ).sortBy(_._2)
    query.list.map(row => (row._1.toString, row._2))
  }


def insert(company: Company)(implicit s:Session){
    Companies.autoInc.insert(company)
  }


val byId = createFinderBy(_.id)


def findById(id: Long)(implicit s:Session): Option[Company] =
      Companies.byId(id).firstOption

}
