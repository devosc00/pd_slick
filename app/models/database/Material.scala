package models

import java.util.Date

import play.api.Play.current

import play.api.db.slick.Config.driver.simple._

import slick.lifted.{Join, MappedTypeMapper}


case class Material(id: Option[Long], m_name: String, amount: Float, t_amount: Float, dates: Option[Date])


object Materials extends Table[Material]("Material") {

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


 
}