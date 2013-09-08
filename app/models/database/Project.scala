package models

import java.util.Date

import play.api.Play.current

import play.api.db.slick.Config.driver.simple._

import slick.lifted.{Join, MappedTypeMapper}



case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class Project(id: Option[Long] = None, name: String, end_date: Option[Date] = None, 
  order: Int, done_parts: Int, mat_counter: Float, company: Option[Long] = None, material: Option[Long] = None)


object Projects extends Table[Project]("PROJECT") {

  implicit val javaUtilDateTypeMapper = MappedTypeMapper.base[java.util.Date, java.sql.Date](
    x => new java.sql.Date(x.getTime),
    x => new java.util.Date(x.getTime)
  )

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def end_date = column[Date]("end_date", O.Nullable)
  def order = column[Int]("order", O.Nullable)
  def done_parts = column[Int]("done_parts", O.Nullable)
  def mat_counter = column[Float]("mat_counter", O.Nullable)
  def company = column[Long]("company", O.Nullable)
  def material = column[Long]("material", O.Nullable)


  def * = id.? ~ name ~ end_date.? ~ order ~ done_parts ~ mat_counter ~ company.? ~ material.? <>(Project.apply _, Project.unapply _)

  def autoInc = * returning id

  val byId = createFinderBy(_.id)
  val byName = createFinderBy(_.name)

  /**
   * Retrieve from the id
   * @param id
   */
  def findById(id: Long)(implicit s:Session): Option[Project] =
      Projects.byId(id).firstOption


  def findByName(name: String)(implicit s: Session): Option[Project] =
  	  Projects.byName(name).firstOption 
  /**
   * Count all
   */
  def count(implicit s:Session): Int =
      Query(Projects.length).first

  /**
   * Count with a filter
   * @param filter
   */
  def count(filter: String)(implicit s:Session) : Int =
      Query(Projects.where(_.name.toLowerCase like filter.toLowerCase).length).first

  /**
   * Return a page of (Project,Material,Company)
   * @param page
   * @param pageSize
   * @param orderBy
   * @param filter
   */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%")(implicit s:Session): 
            Page[(Project, Option[Material], Option[Company])] = {

    val offset = pageSize * page
    val query =
      (for {
        (project, company) <- Projects leftJoin Companies on (_.company === _.id)
        (project, material) <- Projects leftJoin Materials on (_.material === _.id)
        if project.name.toLowerCase like filter.toLowerCase()
      }
      yield (project, material.id.?, company.name.?))
        .drop(offset)
        .take(pageSize)

    val totalRows = count(filter)
    val result = query.list.map(row => (row._1, row._2.map(value => Material(Option(value))), 
    	row._3.map(value => Company(Option(value)))))

    Page(result, page, offset, totalRows)
  }

  /**
   * Insert a new Project
   * @param Project
   */
  def insert(project: Project)(implicit s:Session) {
    Projects.autoInc.insert(project)
  }

  /**
   * Update
   * @param id
   * @param Project
   */
  def update(id: Long, project: Project)(implicit s:Session) {
    val ProjectToUpdate: Project = project.copy(Some(id))
    Projects.where(_.id === id).update(ProjectToUpdate)
  }

  /**
   * Delete a Project
   * @param id
   */
  def delete(id: Long)(implicit s:Session) {
    Projects.where(_.id === id).delete
  }

  def nameToId(name: String)(implicit s:Session): Long = {
  	val p = Projects.findByName(name)
    val id = p.id 
  }

  def findDoneParts(id: Long)(implicit s:Session): Int = {
    val p = Projects.findById(id)
    val done = p.done_parts
  }

  def findEndDate(id: Long)(implicit s:Session): Date = {
    val p = Projects.findById(id)
    val end = p.end_date

  } 


}