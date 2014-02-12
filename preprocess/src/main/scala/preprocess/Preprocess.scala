package preprocess

import java.io._
import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

object Preprocess {
  def main(args: Array[String]) {
    val dictPath = "../dictionary/dictionary.csv"
    val dataPath = "../input/"
    val outputPath = "../output/"
    val dataFile = new File(dataPath).list()
    var dictWords = Map[String, Boolean]()
    var conversations = Map[String, Map[String, ListBuffer[Chat]]]()
    
    /* Load words from dictionary */
    for (word <- io.Source.fromFile(dictPath).getLines) {
      dictWords += word -> true
    }
    
    /* Load conversations of users */
    for (fileName <- dataFile.toList) {
      for (line <- io.Source.fromFile(dataPath + fileName).getLines.drop(1)) {
        val personName = fileName.stripSuffix(".csv")
        val _line = line.split("\",\"")
	    val date = new Date(_line(3).dropRight(1).toLong * 1000L)
	    val format = new SimpleDateFormat("yyyy-MM-dd")
        val day = format.format(date)
      
        if (!conversations.contains(personName)) {
          conversations(personName) = Map[String, ListBuffer[Chat]]()
        }

        if (!conversations(personName).contains(day)) {
          conversations(personName)(day) = ListBuffer[Chat]()
        }
        conversations(personName)(day) += new Chat(_line(1).equals("sent"), _line(2))
      }
    }
    
    /* Evaluate formality */
    for ((person, conversation) <- conversations) {
      val sentFile = new FileOutputStream(new File(outputPath + person + '_' + "sent"), false)
      val recvFile = new FileOutputStream(new File(outputPath + person + '_' + "recv"), false)
      val totalFile = new FileOutputStream(new File(outputPath + person + '_' + "total"), false)
      val sentStream = new PrintStream(sentFile)
      val recvStream = new PrintStream(recvFile)
      val totalStream = new PrintStream(totalFile)
      
      for((day, chats) <- conversation) {
        var countSentFormal = 0
        var countSentTotal = 0
        var countRecvFormal = 0
        var countRecvTotal = 0
        
        for (chat <- chats) {
          if (chat.getIsSent()) {
            for (word <- chat.getChat().split(' ')) {
              if (dictWords.contains(word)) {
                countSentFormal += 1
              }
              countSentTotal += 1
            }
          } else {
            for (word <- chat.getChat().split(' ')) {
              if (dictWords.contains(word)) {
                countRecvFormal += 1
              }
              countRecvTotal += 1
            }
          }
        }
        
        if (countSentTotal != 0) {
          sentStream.print(day + ',' + (countSentFormal.toFloat/countSentTotal*100) + ',' + countSentTotal + '\n')
        } else {
          sentStream.print(day + ',' + 0.toString + '\n')
        }
        
        if (countRecvTotal != 0 ) {
          recvStream.print(day + ',' + (countRecvFormal.toFloat/countRecvTotal*100) + ',' + countRecvTotal + '\n')
        } else {
          recvStream.print(day + ',' + 0.toString + +',' + 1.toString + '\n')
        }
        
        totalStream.print(day + ',' + ((countSentFormal + countRecvFormal.toFloat)/(countRecvTotal + countSentTotal) *100)
            + ',' + (countRecvTotal + countSentTotal) + '\n')
      }
      
      sentStream.close()
      recvStream.close()
      totalStream.close()
    }
  }
}