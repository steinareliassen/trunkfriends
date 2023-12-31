package org.osprey.trunkfriends.opt

// Todo:
// Draft for tests, need to create a large amount of follower / following account
// and then history events afterwards. Should be moved to test folder...
val usedAccounts = mutableListOf<String>()

val domains = listOf(
    "otterpaws", "fisheater", "busybird", "techwizard"
)
val topLevelDomains = listOf(
    "club", "furry", "social", "lgbt", "queer"
)
fun xmain() {

    val timestamp = System.currentTimeMillis()

    /*fun historyLine(userObject: CurrentUser) {
        val fi = if (userObject.follower) "1" else "*"
        val fo = if (userObject.following) "1" else "*"
        val ctrString = "$timestamp-$fi$fo"
        newHistoryLines.add(Pair(userObject, ctrString))
    }*/

    // Follow an initial 15000 animals
    var duplicateDetect = 0
    var firstName: String
    var lastName: String
    var instance: String
    (1..15000).forEach {
        do {
            if (duplicateDetect > 0) println("Duplicate avoided $duplicateDetect for animal number $it")
            firstName = animals[(0..animals.size - 1).random()]
            lastName = animals[(0..animals.size - 1).random()]
            instance = domains[(0..domains.size - 1).random()] +
                    "." + topLevelDomains[(0..topLevelDomains.size - 1).random()]
            duplicateDetect++
        } while (usedAccounts.contains("$firstName$lastName@$instance"))
        duplicateDetect = 0
        usedAccounts.add("$firstName$lastName@$instance")
    }

    Thread.sleep(5000)
    usedAccounts.forEach {
        println(it)
    }
}