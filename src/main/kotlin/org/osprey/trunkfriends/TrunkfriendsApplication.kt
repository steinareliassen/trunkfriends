package org.osprey.trunkfriends

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.StringTokenizer
import kotlin.jvm.optionals.getOrNull

data class UserClass(
	var id : String? = null,
	val acct : String? = null,
	var username : String? = null
)

val timestamp = System.currentTimeMillis()

val initialData = """
+1691926399556,{"id":"110876451052268049","acct":"BoneQueenEve@meow.social","username":"BoneQueenEve"}
+1691926399556,{"id":"109296039321234619","acct":"hildeaustlid@mastodon.online","username":"hildeaustlid"}
+1691926399556,{"id":"109383313176629519","acct":"_Oyvind_@snabelen.no","username":"_Oyvind_"}
+1691926399556,{"id":"109268622546973167","acct":"gamsjo@snabelen.no","username":"gamsjo"}
+1691926399556,{"id":"17296","acct":"geir@snabelen.no","username":"geir"}
+1691926399556,{"id":"110798281694828518","acct":"emdl@snabelen.no","username":"emdl"}
+1691926399556,{"id":"109278943270736975","acct":"tanketom@tutoteket.no","username":"tanketom"}
+1691926399556,{"id":"110843487577837926","acct":"kamuniak@finfur.net","username":"kamuniak"}
+1691926399556,{"id":"110826429626338302","acct":"Laberpferd@sueden.social","username":"Laberpferd"}
+1691926399556,{"id":"110838710647669210","acct":"Alex_Parrot@meow.social","username":"Alex_Parrot"}
+1691926399556,{"id":"110833428279892347","acct":"birhua@mastodon.social","username":"birhua"}
+1691926399556,{"id":"110776913666329489","acct":"RegenTheOwl@furry.engineer","username":"RegenTheOwl"}
+1691926399556,{"id":"109524769074981295","acct":"forteller@tutoteket.no","username":"forteller"}
+1691926399556,{"id":"110823142768118369","acct":"Cal@snake.cool","username":"Cal"}
+1691926399556,{"id":"109548150004759355","acct":"eleos@fuzzytacular.net","username":"eleos"}
+1691926399556,{"id":"110797320901046353","acct":"AvalonJay@squawk.social","username":"AvalonJay"}
+1691926399556,{"id":"110801442907774495","acct":"Evix@squawk.social","username":"Evix"}
+1691926399556,{"id":"110815296457788915","acct":"PuzzledJayPros@meow.social","username":"PuzzledJayPros"}
+1691926399556,{"id":"109553629011359369","acct":"Koch@transfur.social","username":"Koch"}
+1691926399556,{"id":"110660938566543509","acct":"barnibu@birdbutt.com","username":"barnibu"}
+1691926399556,{"id":"110530465307532398","acct":"lyze@meow.social","username":"lyze"}
+1691926399556,{"id":"110810756761517741","acct":"Skulldog@socel.net","username":"Skulldog"}
+1691926399556,{"id":"109366826264440303","acct":"KitMuse@eponaauthor.social","username":"KitMuse"}
+1691926399556,{"id":"109262550267788563","acct":"palmklov@snabelen.no","username":"palmklov"}
+1691926399556,{"id":"110686863130233024","acct":"dkannapan@mastodon.art","username":"dkannapan"}
+1691926399556,{"id":"109587796013520710","acct":"james@bark.lgbt","username":"james"}
+1691926399556,{"id":"110464252473471264","acct":"gull@squawk.social","username":"gull"}
+1691926399556,{"id":"110800007623462441","acct":"OwlstarVera@squawk.social","username":"OwlstarVera"}
+1691926399556,{"id":"110588949454409590","acct":"annantidote@chitter.xyz","username":"annantidote"}
+1691926399556,{"id":"109268104491042177","acct":"Fred_og_ro@snabelen.no","username":"Fred_og_ro"}
+1691926399556,{"id":"110799931532580077","acct":"PriddhasPengu@meow.social","username":"PriddhasPengu"}
+1691926399556,{"id":"110736871364901933","acct":"WeirdRomance@meow.social","username":"WeirdRomance"}
+1691926399556,{"id":"109779639347385961","acct":"gustav_messner@birdbutt.com","username":"gustav_messner"}
+1691926399556,{"id":"109839493218888489","acct":"kite@tech.lgbt","username":"kite"}
+1691926399556,{"id":"109561972311283289","acct":"CatHat@mstdn.party","username":"CatHat"}
+1691926399556,{"id":"110796810661897718","acct":"Blaze@meow.social","username":"Blaze"}
+1691926399556,{"id":"110654068334372262","acct":"Knewfy@meow.social","username":"Knewfy"}
+1691926399556,{"id":"110788152758474921","acct":"evermore@tech.lgbt","username":"evermore"}
+1691926399556,{"id":"109376687988357916","acct":"silvereagle@furry.engineer","username":"silvereagle"}
+1691926399556,{"id":"110770584592528966","acct":"Charter@bark.lgbt","username":"Charter"}
+1691926399556,{"id":"109803588188870456","acct":"routpatt@meow.social","username":"routpatt"}
+1691926399556,{"id":"110768792718985776","acct":"Leophan@meow.social","username":"Leophan"}
+1691926399556,{"id":"109259681729580331","acct":"ishaway@mastodon.art","username":"ishaway"}
+1691926399556,{"id":"109537675023828021","acct":"Silkyfur@mastodon.social","username":"Silkyfur"}
+1691926399556,{"id":"109330169908405691","acct":"Vigdisol@snabelen.no","username":"Vigdisol"}
+1691926399556,{"id":"110378224508266561","acct":"arnfinnp@expressional.social","username":"arnfinnp"}
+1691926399556,{"id":"109922475229418598","acct":"avis_jay@squawk.social","username":"avis_jay"}
+1691926399556,{"id":"109309023694217311","acct":"majatomic@snabelen.no","username":"majatomic"}
+1691926399556,{"id":"109267845718447126","acct":"mortenaa@mastodon.online","username":"mortenaa"}
+1691926399556,{"id":"110652491230067794","acct":"Gaertan@mastodon.social","username":"Gaertan"}
+1691926399556,{"id":"109352596354432315","acct":"thomasrost@oslo.town","username":"thomasrost"}
+1691926399556,{"id":"109393410590322101","acct":"Bwee@meow.social","username":"Bwee"}
+1691926399556,{"id":"110689448798183625","acct":"Tya@toot.cat","username":"Tya"}
+1691926399556,{"id":"109375010989020856","acct":"Collings@fursuits.online","username":"Collings"}
+1691926399556,{"id":"110713758820254871","acct":"Etath@vivaldi.net","username":"Etath"}
+1691926399556,{"id":"110713544269369527","acct":"corraven@mastodon.social","username":"corraven"}
+1691926399556,{"id":"109559755540889092","acct":"emptyother@oslo.town","username":"emptyother"}
+1691926399556,{"id":"109436636546855523","acct":"anxiousMofo@sself.co","username":"anxiousMofo"}
+1691926399556,{"id":"17610","acct":"jackyan@mastodon.social","username":"jackyan"}
+1691926399556,{"id":"110664719345238765","acct":"foreverVoyager@bears.town","username":"foreverVoyager"}
+1691926399556,{"id":"110165329861922698","acct":"Melody@squawk.social","username":"Melody"}
+1691926399556,{"id":"109298993698602422","acct":"tark@tech.lgbt","username":"tark"}
+1691926399556,{"id":"109830457918866837","acct":"aetus@birdbutt.com","username":"aetus"}
+1691926399556,{"id":"109363096437621707","acct":"EricMalves@squawk.social","username":"EricMalves"}
+1691926399556,{"id":"109303668618969737","acct":"ouiji@chitter.xyz","username":"ouiji"}
+1691926399556,{"id":"109577645690831912","acct":"TinyDragonArtist@mastodon.art","username":"TinyDragonArtist"}
+1691926399556,{"id":"25542","acct":"Hawlucha@donphan.social","username":"Hawlucha"}
+1691926399556,{"id":"109366539078102877","acct":"KingDeadWolf@meow.social","username":"KingDeadWolf"}
+1691926399556,{"id":"110119564073814281","acct":"BrujoFaolan@meow.social","username":"BrujoFaolan"}
+1691926399556,{"id":"110667833966303358","acct":"naunan@mastodon.social","username":"naunan"}
+1691926399556,{"id":"108210369544135085","acct":"marius851000@mariusdavid.fr","username":"marius851000"}
+1691926399556,{"id":"109376703933810294","acct":"backpaw@meow.social","username":"backpaw"}
+1691926399556,{"id":"109831788866386313","acct":"shadari@meow.social","username":"shadari"}
+1691926399556,{"id":"109525688148238183","acct":"oceanity@meow.social","username":"oceanity"}
+1691926399556,{"id":"110641154421001397","acct":"ivorycrow@squawk.social","username":"ivorycrow"}
+1691926399556,{"id":"109543842925712134","acct":"KatSteelwing@squawk.social","username":"KatSteelwing"}
+1691926399556,{"id":"110647564927527928","acct":"jaffa@furries.club","username":"jaffa"}
+1691926399556,{"id":"109560568131357522","acct":"Maki@furry.engineer","username":"Maki"}
+1691926399556,{"id":"109461167809938645","acct":"alopex@fursuits.online","username":"alopex"}
+1691926399556,{"id":"109401038144194802","acct":"Zypheran@meow.social","username":"Zypheran"}
+1691926399556,{"id":"109317659500974268","acct":"CheetahObscura@meow.social","username":"CheetahObscura"}
+1691926399556,{"id":"110590895977161623","acct":"willow_ww@meow.social","username":"willow_ww"}
+1691926399556,{"id":"110640345720597679","acct":"zai@bark.lgbt","username":"zai"}
+1691926399556,{"id":"110489250228615977","acct":"skyler@furry.engineer","username":"skyler"}
+1691926399556,{"id":"109405879471098561","acct":"Stax@furry.engineer","username":"Stax"}
+1691926399556,{"id":"109354238132092767","acct":"helloboing@meow.social","username":"helloboing"}
+1691926399556,{"id":"109384595998619546","acct":"Crow_Crow@tech.lgbt","username":"Crow_Crow"}
+1691926399556,{"id":"109308362637458126","acct":"Tricad@meow.social","username":"Tricad"}
+1691926399556,{"id":"108392625770760867","acct":"notthatdelta@mstdn.social","username":"notthatdelta"}
+1691926399556,{"id":"109299631564753839","acct":"korikitsune@mastodon.social","username":"korikitsune"}
+1691926399556,{"id":"110610695922363511","acct":"Snowyowl7v@birdbutt.com","username":"Snowyowl7v"}
+1691926399556,{"id":"109843123316664702","acct":"KeironWedlin@meow.social","username":"KeironWedlin"}
+1691926399556,{"id":"110640399829825802","acct":"Ume@squawk.social","username":"Ume"}
+1691926399556,{"id":"110640376926721462","acct":"vinfurr@oslo.town","username":"vinfurr"}
+1691926399556,{"id":"110391828013018764","acct":"seniorsguidetocomputers@twit.social","username":"seniorsguidetocomputers"}
+1691926399556,{"id":"109371677689231949","acct":"peterdeppisch@mstdn.ca","username":"peterdeppisch"}
+1691926399556,{"id":"110595998158242103","acct":"prios@transfur.social","username":"prios"}
+1691926399556,{"id":"109872891448327623","acct":"bacchus1234@mastodon.social","username":"bacchus1234"}
+1691926399556,{"id":"109562935139255629","acct":"StingrayBadger@zirk.us","username":"StingrayBadger"}
+1691926399556,{"id":"109677074487152016","acct":"jaffa@vulpine.club","username":"jaffa"}
+1691926399556,{"id":"109382359405909456","acct":"avon_deer@dragonchat.org","username":"avon_deer"}
+1691926399556,{"id":"110169206481631300","acct":"Sebastian@kangaroo.to","username":"Sebastian"}
+1691926399556,{"id":"110165759468212387","acct":"darkfox@tech.lgbt","username":"darkfox"}
+1691926399556,{"id":"110164219115742346","acct":"reyna@furry.engineer","username":"reyna"}
+1691926399556,{"id":"110153205743786088","acct":"gryphon@dragonchat.org","username":"gryphon"}
+1691926399556,{"id":"109800658992632782","acct":"zyuuzinloveyou@kemonodon.club","username":"zyuuzinloveyou"}
+1691926399556,{"id":"109820525038912582","acct":"Cocoeagle@birdbutt.com","username":"Cocoeagle"}
+1691926399556,{"id":"109820688120909905","acct":"artyewok@pawb.fun","username":"artyewok"}
+1691926399556,{"id":"109466600234254245","acct":"gigu@kolektiva.social","username":"gigu"}
+1691926399556,{"id":"109326319881484241","acct":"StrongFort@infosec.exchange","username":"StrongFort"}
+1691926399556,{"id":"109362258811207914","acct":"susanneleist@mastodon.social","username":"susanneleist"}
+1691926399556,{"id":"109474792150481234","acct":"eric@ericpuryear.com","username":"eric"}
+1691926399556,{"id":"109348495265286869","acct":"Romston@mastodon.online","username":"Romston"}
+1691926399556,{"id":"109596956406276960","acct":"russell_but@mastodon.scot","username":"russell_but"}
+1691926399556,{"id":"109495621411838144","acct":"NotMyBub@beekeeping.ninja","username":"NotMyBub"}
+1691926399556,{"id":"109368427524391635","acct":"SecularJeffrey@ohai.social","username":"SecularJeffrey"}
+1691926399556,{"id":"109580699064781403","acct":"helpmi@bark.lgbt","username":"helpmi"}
+1691926399556,{"id":"107694101714378570","acct":"nall@dook.business","username":"nall"}
+1691926399556,{"id":"109546779876410994","acct":"kootenay@fursuits.online","username":"kootenay"}
+1691926399556,{"id":"109330999820164728","acct":"cheetah_spottycat@toot.cat","username":"cheetah_spottycat"}
+1691926399556,{"id":"109372383050656559","acct":"TwospotzSWE@artisan.chat","username":"TwospotzSWE"}
+1691926399556,{"id":"109303949159073452","acct":"darkfox@vulpine.club","username":"darkfox"}
+1691926399556,{"id":"109536548532630180","acct":"shadowfox_de@meow.social","username":"shadowfox_de"}
+1691926399556,{"id":"109536443531728468","acct":"Mithy@meow.social","username":"Mithy"}
+1691926399556,{"id":"109366156934609677","acct":"Purrtail@pawb.fun","username":"Purrtail"}
+1691926399556,{"id":"109366929610350174","acct":"ScalerandiArt@mastodon.art","username":"ScalerandiArt"}
+1691926399556,{"id":"109528413118771482","acct":"GlideOsprey@meow.social","username":"GlideOsprey"}
+1691926399556,{"id":"109404254212886497","acct":"SlyCat@meow.social","username":"SlyCat"}
+1691926399556,{"id":"109470855371778346","acct":"chipperwolf@meow.social","username":"chipperwolf"}
+1691926399556,{"id":"109502418600705388","acct":"Hachi@meow.social","username":"Hachi"}
+1691926399556,{"id":"109484439140614937","acct":"sebkha@meow.social","username":"sebkha"}
+1691926399556,{"id":"109307610515158440","acct":"Schiraki@mastodon.art","username":"Schiraki"}
+1691926399556,{"id":"109396758431715210","acct":"virgil@tealich.com","username":"virgil"}
+1691926399556,{"id":"109377765768135440","acct":"DoddsieFox@meow.social","username":"DoddsieFox"}
+1691926399556,{"id":"109417930564483596","acct":"frozenfoxx@vulpine.club","username":"frozenfoxx"}
+1691926399556,{"id":"109356545963991880","acct":"nu@pl.hyperboreal.zone","username":"nu"}
+1691926399556,{"id":"109451217913507417","acct":"tsaroafterdark@tsarolion.com","username":"tsaroafterdark"}
+1691926399556,{"id":"109428470506841784","acct":"tsaro@tsarolion.com","username":"tsaro"}
+1691926399556,{"id":"109294649845734980","acct":"Yotenotes@meow.social","username":"Yotenotes"}
+1691926399556,{"id":"109305654089742861","acct":"nemereth@meow.social","username":"nemereth"}
+1691926399556,{"id":"109473246492929456","acct":"SebastianSilverfox@vulpine.club","username":"SebastianSilverfox"}
+1691926399556,{"id":"109366501986031940","acct":"Domiborealis@meow.social","username":"Domiborealis"}
+1691926399556,{"id":"109292103046733599","acct":"Dmeyers@mastodon.online","username":"Dmeyers"}
+1691926399556,{"id":"109354259505882498","acct":"bmkrohn1@fribygda.no","username":"bmkrohn1"}
+1691926399556,{"id":"109381375333667667","acct":"finch@squawk.social","username":"finch"}
+1691926399556,{"id":"109383188811713384","acct":"panduck@fins.fish","username":"panduck"}
+1691926399556,{"id":"109375447510718469","acct":"aurorapenguin@furry.engineer","username":"aurorapenguin"}
+1691926399556,{"id":"109371134756367522","acct":"Velux@squeaky.social","username":"Velux"}
+1691926399556,{"id":"109367405901449952","acct":"dogindenial@critter.zone","username":"dogindenial"}
+1691926399556,{"id":"109367467394414926","acct":"BluefoxLongtail@pawb.fun","username":"BluefoxLongtail"}
+1691926399556,{"id":"109366742686060770","acct":"prios@pettingzoo.co","username":"prios"}
+1691926399556,{"id":"19206","acct":"asonix@masto.asonix.dog","username":"asonix"}
+1691926399556,{"id":"109251962994205103","acct":"berkanowolf@meow.social","username":"berkanowolf"}
+1691926399556,{"id":"109288133586224675","acct":"Akylian@meow.social","username":"Akylian"}
+1691926399556,{"id":"109363809725766423","acct":"Prios@mastodon.social","username":"Prios"}
+1691926399556,{"id":"109362026204343860","acct":"artyewok","username":"artyewok"}
+1691926399556,{"id":"109360871640975090","acct":"gullvinge@samenet.social","username":"gullvinge"}
+1691926399556,{"id":"109300738598024719","acct":"stuffedpanda@meow.social","username":"stuffedpanda"}
+1691926399556,{"id":"109328060479372118","acct":"EnginDeer@mastodon.art","username":"EnginDeer"}
+1691926399556,{"id":"109326322771832234","acct":"blucao@meow.social","username":"blucao"}
+1691926399556,{"id":"109319088186165791","acct":"smevog@spragleknas.no","username":"smevog"}
+1691926399556,{"id":"109322191686841813","acct":"artyewok@mastodon.social","username":"artyewok"}
+1691926399556,{"id":"109296864228446605","acct":"BlinkingRight","username":"BlinkingRight"}
+1691926399556,{"id":"109295129634852835","acct":"alexmichael@mastodon.art","username":"alexmichael"}
+1691926399556,{"id":"109308624102528373","acct":"Cursico@artisan.chat","username":"Cursico"}
+1691926399556,{"id":"109293688627414997","acct":"Thornwolf@mastodon.art","username":"Thornwolf"}
+1691926399556,{"id":"109302832900470260","acct":"Skulldog@mastodon.art","username":"Skulldog"}
+1691926399556,{"id":"109296145844155564","acct":"darksecond@chaos.social","username":"darksecond"}
+1691926399556,{"id":"109290168368435958","acct":"neweinstein@meow.social","username":"neweinstein"}
+1691926399556,{"id":"109296879319390239","acct":"foxelifox@meow.social","username":"foxelifox"}
+1691926399556,{"id":"109277350851694213","acct":"xchris@tech.lgbt","username":"xchris"}
+1691926399556,{"id":"108204843128997280","acct":"kanrei@mastodon.art","username":"kanrei"}
+1691926399556,{"id":"109257178305659629","acct":"spocha@mastodon.art","username":"spocha"}
+1691926399556,{"id":"109288053573603751","acct":"kootenay@mastodon.social","username":"kootenay"}
+1691926399556,{"id":"109286385956655987","acct":"Swandog@mastodon.art","username":"Swandog"}
+1691926399556,{"id":"109247393063023034","acct":"Kyreeth@dragon.style","username":"Kyreeth"}
""".trimIndent()

fun findUserPage(start : Long, id : String) : Pair<Array<UserClass>, Long> {
	val request = HttpRequest.newBuilder()
		.uri(URI.create(
			"https://mastodon.green/api/v1/accounts/${id}/followers" +
					if (start != 0L) "?max_id=$start" else ""))
		.header("Authorization", bearer)
		.method("GET", HttpRequest.BodyPublishers.noBody())
		.build()

	val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

	val mapper = jacksonObjectMapper()
		.configure(
			DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
			false
		)

	val users = mapper.readValue(response.body(), Array<UserClass>::class.java)

	users.forEachIndexed { index, userClass ->
		println("+$timestamp,${mapper.writeValueAsString(userClass)}")
	}

	val header = response.headers().firstValue("Link").getOrNull() ?: "empty"
	val startIndex = header.indexOf("max_id=")
	val stopIndex = header.indexOf(">")

	Thread.sleep(500)
	try {
		val next = header.substring(startIndex + 7, stopIndex).toLong()
		return Pair(users, next)
	} catch (e : java.lang.NumberFormatException) {}
	return Pair(users, 0L)

}

fun main(args: Array<String>) {

	val tokenizer = StringTokenizer(initialData)
	tokenizer.countTokens()
	val request: HttpRequest = HttpRequest.newBuilder()
		.uri(URI.create("https://mastodon.green/api/v1/accounts/verify_credentials"))
		.header("Authorization", bearer)
		.method("GET", HttpRequest.BodyPublishers.noBody())
		.build()

	val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

	val mapper = jacksonObjectMapper()
		.configure(
			DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
			false
		)
	val user = mapper.readValue(
			response.body(),
			UserClass::class.java
		)

	val mutablelist = mutableListOf<UserClass>()
	var list = findUserPage(0,user.id ?: "empty")
	mutablelist.addAll(list.first)
	while (list.second != 0L) {
		list = findUserPage(list.second, user.id ?: "empty")
		mutablelist.addAll(list.first)
	}

	println(mutablelist.size)
}
