package ecommerce.system

import _root_.infrastructure.akka.SerializationSupport
import akka.actor.{ ActorRef, ActorSystem }
import ddd.support.domain.Message
import ddd.support.domain.command.{ Command, CommandMessage }
import ddd.support.domain.protocol.Receipt

import scala.language.implicitConversions
import scala.reflect.ClassTag

object DeliveryContext {

  case object ReceiptsRequested
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

      /**
       * dlr - delivery receipt
       */
      def requestDLR[A](implicit t: ClassTag[A]) = {
        msg.withMetaAttribute(ReceiptsRequested,
          msg.tryGetMetaAttribute[Set[Class[_]]](ReceiptsRequested)
            .getOrElse(Set[Class[_]]()).+(t.runtimeClass))
      }
    }

  }

  implicit def toReadonlyConfirmable(message: Message)(implicit system: ActorSystem) = {
    new ReadonlyConfirmable(message)
  }

  class ReadonlyConfirmable(srcMsg: Message)(implicit _system: ActorSystem) extends SerializationSupport {

    override protected def system = _system

    def anyReceiptRequested: Boolean = {
      srcMsg.hasMetaAttribute(ReceiptsRequested)
    }

    def receiptRequested(receipt: Receipt): Boolean = {
      srcMsg.tryGetMetaAttribute[Set[Class[_]]](ReceiptsRequested)
        .exists(_.contains(receipt.getClass))
    }

    def receiptRequester: ActorRef = deserialize(srcMsg.getMetaAttribute(ReceiptRequester))

    def sendReceiptIfRequested(receipt: Receipt): Unit = {
      if (receiptRequested(receipt)) {
        receiptRequester ! receipt
      }
    }
  }

}
