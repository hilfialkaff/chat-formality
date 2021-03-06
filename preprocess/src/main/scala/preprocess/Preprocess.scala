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
        val _line = line.replaceAll("[^a-zA-Z0-9,\" ]", "").split("\",\"")
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
      val outFile = new FileOutputStream(new File(outputPath + person), false)
      val outStream = new PrintStream(outFile)
      val totalFile = new FileOutputStream(new File(outputPath + person + "_total"), false)
      val totalStream = new PrintStream(totalFile)
      var prevSentFormalityScore = 0.0
      var prevRecvFormalityScore = 0.0
      var sentFormalityScore = 0.0
      var recvFormalityScore = 0.0
      
      for(day <- conversation.keys.toSeq.sorted) {
        val chats = conversation(day)
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
        
        if (countSentTotal < 10) {
          println()
          sentFormalityScore = prevSentFormalityScore
        } else {
          sentFormalityScore = (countSentFormal.toFloat/countSentTotal*100)          
          prevSentFormalityScore = sentFormalityScore
        }
        
        if (countRecvTotal < 10) {
          recvFormalityScore = prevRecvFormalityScore
        } else {
          recvFormalityScore = (countRecvFormal.toFloat/countRecvTotal*100)
          prevRecvFormalityScore = recvFormalityScore
        }
        
        outStream.print(day + ',' + sentFormalityScore + ',' + countSentTotal + ',' + recvFormalityScore
            + ',' + countRecvTotal + '\n')
        totalStream.print(day + ",total," + (recvFormalityScore * countRecvTotal / (countRecvTotal + countSentTotal) +
            sentFormalityScore * countSentTotal / (countRecvTotal + countSentTotal))
            + ',' + (countRecvTotal + countSentTotal) + '\n')
      }
      
      outStream.close()
      totalStream.close()
    }
  }
}