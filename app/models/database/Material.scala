package models

import java.util.Date

import play.api.Play.current

import play.api.db.slick.Config.driver.simple._

import slick.lifted.{Join, MappedTypeMapper}


case class Material(id: Option[Long], m_name: String, amount: Float, t_amount: Float, dates: Option[Date])


object Materials extends Table[Material]("MATERIAL") {

	implicit val javaUtilDateTypeMapper = MappedTypeMapper.base[java.util.Date, java.sql.Date](
    	x => new java.sql.Date(x.getTime),
    	x => new java.util.Date(x.getTime)
  	)


  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def m_name = column[String]("m_name", O.NotNull)
  def amount = column[Float]("amount", O.Nullable)
  def t_amount = column[Float]("t_amount", O.Nullable)
  def dates = column[Date]("dates", O.Nullable)
  def * = id.? ~ m_name ~ amount ~ t_amount ~ dates.? <>(Material.apply _, Material.unapply _)

  def autoInc = * returning id


  /**
   * Count all
   */
  def count(implicit s:Session): Int =
      Query(Materials.length).first

  /**
   * Count with a filter
   * @param filter
   */
  def count(filter: String)(implicit s:Session) : Int =
      Query(Materials.where(_.m_name.toLowerCase like filter.toLowerCase).length).first


  def options(implicit s:Session): Seq[(String, String)] = {
    val query = (for {
      mat <- Materials
    } yield (mat.id, mat.m_name)
      ).sortBy(_._2)
    query.list.map(row => (row._1.toString, row._2))
  }

    def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%")(implicit s:Session): 
            Page[Material] = {

    val offset = pageSize * page
    val query =
      (for (
         mat <- Materials 
        if mat.m_name.toLowerCase like filter.toLowerCase()
      )
      yield (mat))
        .drop(offset)
        .take(pageSize)

    val totalRows = count(filter)
    val result = query.list
    

    Page(result, page, offset, totalRows)
  }
 
}