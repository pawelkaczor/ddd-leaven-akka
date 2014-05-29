package ecommerce.system

import _root_.infrastructure.akka.SerializationSupport
import ddd.support.domain.Message
import akka.actor.{ActorSystem, ActorRef}
import scala.language.implicitConversions
import ddd.support.domain.command.{Command, CommandMessage}
import ddd.support.domain.protocol.Delivered

object DeliveryContext {

  case object ReceiptRequested
  case object ReceiptRequester
  case object ReceiptMsg

  object Adjust {

    implicit def toCommandMessage(command: Command)(implicit system: ActorSystem) = new Confirmable(CommandMessage(command))

    implicit def toConfirmable(message: Message)(implicit system: ActorSystem) = {
      new Confirmable(message)
    }

    
    case class Confirmable(msg: Message)(implicit _system: ActorSystem) extends ReadonlyConfirmable(msg) {

      def withReceiptRequester(requester: ActorRef) = {
        msg.withMetaAttribute(ReceiptRequester, serialize(requester))
      }

      def withReceipt(receiptMsg: Delivered) = {
        msg.withMetaAttribute(ReceiptMsg, receiptMsg)
      }

      /**
       * dlr - delivery receipt
       */
      def requestDLR() = {
        msg.withMetaAttribute(ReceiptRequested, true)
      }
    }

  }

  
  implicit def toReadonlyConfirmable(message: Message)(implicit system: ActorSystem) = {
    new ReadonlyConfirmable(message)
  }

  class ReadonlyConfirmable(srcMsg: Message)(implicit _system: ActorSystem) extends SerializationSupport {

    override protected def system = _system

    def receiptRequested = srcMsg.hasMetaAttribute(ReceiptRequested)

    def receiptRequester: ActorRef = deserialize(srcMsg.getMetaAttribute(ReceiptRequester))

    def receipt: Delivered = srcMsg.tryGetMetaAttribute(ReceiptMsg).getOrElse(Delivered)
  }


}
