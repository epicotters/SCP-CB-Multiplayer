Global BurntNote%

Const MaxItemAmount% = 10
Global ItemAmount%
Dim Inventory.Items(MaxItemAmount + 1)
Global InvSelect%, SelectedItem.Items

Global ClosestItem.Items

Type ItemTemplates
	Field name$
	Field tempname$
	
	Field sound%
	
	Field found%
	
	Field obj%
	Field invimg%
	Field imgpath$, img%
	
	Field scale#
	Field bumptex%
	Field tex%
End Type 

Function CreateItemTemplate.ItemTemplates(name$, tempname$, objpath$, invimgpath$, imgpath$, scale#, texturepath$ = "", bump$="")
	Local it.ItemTemplates = New ItemTemplates, n
	
	it\obj = LoadMesh(objpath)
	
	Local texture%
	
	If texturepath <> "" Then
		texture = LoadTexture(texturepath)
		EntityTexture it\obj, texture
	EndIf 
	
	it\scale = scale
	ScaleEntity(it\obj, scale, scale, scale)
	
	it\invimg = LoadImage(invimgpath)
	MaskImage(it\invimg, 255, 0, 255)
	
	it\imgpath = imgpath
	;If it\imgpath <> "" And it\tempname<>"paper" Then
	;	it\img = LoadImage(imgpath)
	;	MaskImage(it\img, 255, 0, 255)
	;EndIf
	
	it\tempname = tempname
	it\name = name
	
	it\sound = 1
	
	EntityRadius it\obj, 0.01
	EntityPickMode it\obj, 3, False
	MakeCollBox(it\obj)
	
	HideEntity it\obj
	
	Return it
	
End Function 

Function InitItemTemplates()
	Local it.ItemTemplates
	
	it = CreateItemTemplate("Some SCP-420-J", "420", "GFX\items\420.x", "GFX\items\INV420.jpg", "", 0.0005)
	it\sound = 2
	
	CreateItemTemplate("Level 1 Key Card", "key1",  "GFX\items\keycard.x", "GFX\items\INVkey1.jpg", "", 0.0004,"GFX\items\keycard1.jpg")
	CreateItemTemplate("Level 2 Key Card", "key2",  "GFX\items\keycard.x", "GFX\items\INVkey2.jpg", "", 0.0004,"GFX\items\keycard2.jpg")
	CreateItemTemplate("Level 3 Key Card", "key3",  "GFX\items\keycard.x", "GFX\items\INVkey3.jpg", "", 0.0004,"GFX\items\keycard3.jpg")
	CreateItemTemplate("Level 4 Key Card", "key4",  "GFX\items\keycard.x", "GFX\items\INVkey4.jpg", "", 0.0004,"GFX\items\keycard4.jpg")
	CreateItemTemplate("Level 5 Key Card", "key5", "GFX\items\keycard.x", "GFX\items\INVkey5.jpg", "", 0.0004,"GFX\items\keycard5.jpg")
	CreateItemTemplate("Playing Card", "misc", "GFX\items\keycard.x", "GFX\items\INVcard.jpg", "", 0.0004,"GFX\items\card.jpg")
	CreateItemTemplate("Mastercard", "misc", "GFX\items\keycard.x", "GFX\items\INVmastercard.jpg", "", 0.0004,"GFX\items\mastercard.jpg")
	CreateItemTemplate("Key Card Omni", "key6", "GFX\items\keycard.x", "GFX\items\INVkeyomni.jpg", "", 0.0004,"GFX\items\keycardomni.jpg")
	
	it = CreateItemTemplate("Document SCP-079", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\doc079.jpg", 0.003) : it\sound = 0
	it = CreateItemTemplate("Document SCP-895", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\doc895.jpg", 0.003) : it\sound = 0 
	it = CreateItemTemplate("Document SCP-860", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\doc860.jpg", 0.003) : it\sound = 0 	
	it = CreateItemTemplate("SCP-093 Recovered Materials", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\doc093rm.jpg", 0.003) : it\sound = 0 	
	it = CreateItemTemplate("Document SCP-106", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\doc106.jpg", 0.003) : it\sound = 0	
	it = CreateItemTemplate("Document SCP-682", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\doc682.jpg", 0.003) : it\sound = 0
	it = CreateItemTemplate("Document SCP-173", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\doc173.jpg", 0.003) : it\sound = 0
	it = CreateItemTemplate("Document SCP-372", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\doc372.jpg", 0.003) : it\sound = 0
	it = CreateItemTemplate("Document SCP-049", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\doc049.jpg", 0.003) : it\sound = 0
	it = CreateItemTemplate("Document SCP-096", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\doc096.jpg", 0.003) : it\sound = 0
	it = CreateItemTemplate("Document SCP-008", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\doc008.jpg", 0.003) : it\sound = 0
	it = CreateItemTemplate("Document SCP-012", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\doc012.jpg", 0.003) : it\sound = 0
	it = CreateItemTemplate("Document SCP-714", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\doc714.jpg", 0.003) : it\sound = 0
	
	it = CreateItemTemplate("Nuclear Device Document", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\docNDP.jpg", 0.003) : it\sound = 0	
	it = CreateItemTemplate("Class D Orientation Leaflet", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\docORI.jpg", 0.003) : it\sound = 0	
	
	it = CreateItemTemplate("Note from Daniel", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\docdan.jpg", 0.003) : it\sound = 0			
	
	it = CreateItemTemplate("Burnt Note", "paper", "GFX\items\paper.x", "GFX\items\INVbn.jpg", "GFX\items\bn.it", 0.003)
	it\img = BurntNote : it\sound = 0
	
	it = CreateItemTemplate("Mysterious Note", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\sn.it", 0.003) : it\sound = 0	
	
	it = CreateItemTemplate("Mobile Task Forces", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\docMTF.jpg", 0.003) : it\sound = 0
	it = CreateItemTemplate("Mobile Task Force Epsilon-11", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\docNTF.jpg", 0.003) : it\sound = 0
	it = CreateItemTemplate("Security Clearance Levels", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\docSC.jpg", 0.003) : it\sound = 0
	it = CreateItemTemplate("Object Classes", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\docOBJC.jpg", 0.003) : it\sound = 0
	it = CreateItemTemplate("Document", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\docRAND3.jpg", 0.003) : it\sound = 0 
	it = CreateItemTemplate("Note", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\docRAND2.jpg", 0.003) : it\sound = 0 
	it = CreateItemTemplate("Notification", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\docRAND1.jpg", 0.003) :it\sound = 0 	
	it = CreateItemTemplate("Incident Report SCP-106-0204", "paper", "GFX\items\paper.x", "GFX\items\INVpaper.jpg", "GFX\items\docIR106.jpg", 0.003) : it\sound = 0 
	
	it = CreateItemTemplate("Ballistic Vest", "vest", "GFX\items\vest.x", "GFX\items\INVvest.jpg", "", 0.02,"GFX\npcs\MTFbody.jpg")
	it\sound = 2
	it = CreateItemTemplate("Heavy Ballistic Vest", "finevest", "GFX\items\vest.x", "GFX\items\INVvest.jpg", "", 0.022,"GFX\npcs\MTFbody.jpg")
	it\sound = 2
	it = CreateItemTemplate("Bulky Ballistic Vest", "veryfinevest", "GFX\items\vest.x", "GFX\items\INVvest.jpg", "", 0.025,"GFX\npcs\MTFbody.jpg")
	it\sound = 2
	
	it = CreateItemTemplate("Hazmat Suit", "hazmatsuit", "GFX\items\hazmat.b3d", "GFX\items\INVhazmat.jpg", "", 0.013)
	it\sound = 2
	it = CreateItemTemplate("Mysterious Hazmat Suit", "hazmatsuit2", "GFX\items\hazmat.b3d", "GFX\items\INVhazmat.jpg", "", 0.013)
	it\sound = 2
	
	CreateItemTemplate("SCP-500-01", "scp500", "GFX\items\pill.b3d", "GFX\items\INVpill.jpg", "", 0.0010)
	
	it = CreateItemTemplate("First Aid Kit", "firstaid", "GFX\items\firstaid.x", "GFX\items\INVfirstaid.jpg", "", 0.05)
	it = CreateItemTemplate("Small First Aid Kit", "finefirstaid", "GFX\items\firstaid.x", "GFX\items\INVfirstaid.jpg", "", 0.03)
	it = CreateItemTemplate("Blue First Aid Kit", "firstaid2", "GFX\items\firstaid.x", "GFX\items\INVfirstaid2.jpg", "", 0.03, "GFX\items\firstaidkit2.jpg")
	it = CreateItemTemplate("Strange Bottle", "veryfinefirstaid", "GFX\items\eyedrops.b3d", "GFX\items\INVbottle.jpg", "", 0.002, "GFX\items\bottle.jpg")	
	
	it = CreateItemTemplate("Gas Mask", "gasmask", "GFX\items\gasmask.b3d", "GFX\items\INVgasmask.jpg", "", 0.02) : it\sound = 2
	
	it = CreateItemTemplate("Gas Mask", "supergasmask", "GFX\items\gasmask.b3d", "GFX\items\INVgasmask.jpg", "", 0.021) : it\sound = 2
	
	CreateItemTemplate("9V Battery", "bat", "GFX\items\battery.x", "GFX\items\INVbattery.jpg", "", 0.0002)
	
	it = CreateItemTemplate("Origami", "misc", "GFX\items\origami.b3d", "GFX\items\INVorigami.jpg", "", 0.003) : it\sound = 0
	
	CreateItemTemplate("Electronical components", "misc", "GFX\items\electronics.x", "GFX\items\INVelectronics.jpg", "", 0.0011)
	
	CreateItemTemplate("S-NAV 300 Navigator", "nav", "GFX\items\navigator.x", "GFX\items\INVnavigator.jpg", "GFX\items\navigator.png", 0.0011)
	CreateItemTemplate("S-NAV Navigator", "nav", "GFX\items\navigator.x", "GFX\items\INVnavigator.jpg", "GFX\items\navigator.png", 0.0011)
	CreateItemTemplate("S-NAV Navigator Ultimate", "nav", "GFX\items\navigator.x", "GFX\items\INVnavigator.jpg", "GFX\items\navigator.png", 0.0011)
	CreateItemTemplate("S-NAV 310 Navigator", "nav", "GFX\items\navigator.x", "GFX\items\INVnavigator.jpg", "GFX\items\navigator.png", 0.0011)
	
	CreateItemTemplate("Radio Transceiver", "radio", "GFX\items\radio.x", "GFX\items\INVradio.jpg", "GFX\items\radio.png", 0.0010)
	CreateItemTemplate("Radio Transceiver", "fineradio", "GFX\items\radio.x", "GFX\items\INVradio.jpg", "GFX\items\radio.png", 0.0010)
	CreateItemTemplate("Radio Transceiver", "veryfineradio", "GFX\items\radio.x", "GFX\items\INVradio.jpg", "GFX\items\radio.png", 0.0010)
	CreateItemTemplate("Radio Transceiver", "18vradio", "GFX\items\radio.x", "GFX\items\INVradio.jpg", "GFX\items\radio.png", 0.0012)
	
	it = CreateItemTemplate("Cigarette", "cigarette", "GFX\items\420.x", "GFX\items\INV420.jpg", "", 0.0004) : it\sound = 2
	
	it = CreateItemTemplate("Joint", "420s", "GFX\items\420.x", "GFX\items\INV420.jpg", "", 0.0004) : it\sound = 2
	
	it = CreateItemTemplate("Smelly Joint", "420s", "GFX\items\420.x", "GFX\items\INV420.jpg", "", 0.0004) : it\sound = 2
	
	CreateItemTemplate("18V Battery", "18vbat", "GFX\items\battery.x", "GFX\items\INVbattery.jpg", "", 0.0003)
	
	CreateItemTemplate("Strange Battery", "killbat", "GFX\items\battery.x", "GFX\items\INVbattery.jpg", "", 0.0003)
	CreateItemTemplate("Eyedrops", "fineeyedrops", "GFX\items\eyedrops.b3d", "GFX\items\INVeyedrops.jpg", "", 0.0012)
	CreateItemTemplate("Eyedrops", "supereyedrops", "GFX\items\eyedrops.b3d", "GFX\items\INVeyedrops.jpg", "", 0.0012)
	CreateItemTemplate("ReVision Eyedrops", "eyedrops","GFX\items\eyedrops.b3d", "GFX\items\INVeyedrops.jpg", "", 0.0012)
	CreateItemTemplate("RedVision Eyedrops", "eyedrops", "GFX\items\eyedrops.b3d", "GFX\items\INVeyedropsred.jpg", "", 0.0012,"GFX\items\eyedropsred.jpg")
	
	it = CreateItemTemplate("SCP-714", "scp714", "GFX\items\scp714.b3d", "GFX\items\INV714.jpg", "", 0.3)
	it\sound = 2
	
	it = CreateItemTemplate("SCP-1025", "scp1025", "GFX\items\scp1025.b3d", "GFX\items\INV1025.jpg", "", 0.1)
	it\sound = 0
	
End Function 



Type Items
	Field obj%
	Field itemtemplate.ItemTemplates
	Field DropSpeed#
	
	Field level
	
	Field SoundChn%
	
	Field dist#, disttimer#
	
	Field state#, state2#
	
	Field Picked%
End Type 

Function CreateItem.Items(name$, tempname$, x#, y#, z#);, objpath$, invimgpath$, imgpath$ = "", scale#, bump$="")
	
	Local i.Items = New Items, it.ItemTemplates
	
	name = Lower(name)
	tempname = Lower (tempname)
	
	For it.ItemTemplates = Each ItemTemplates
		If Lower(it\name) = name And Lower(it\tempname) = tempname Then
			i\itemtemplate = it
			i\obj = CopyEntity(it\obj)
			ShowEntity i\obj
		EndIf
	Next 
	
	If i\itemtemplate = Null Then RuntimeError("Item template not found ("+name+", "+tempname+")")
	
	ResetEntity i\obj		
	PositionEntity(i\obj, x, y, z)
	RotateEntity (i\obj, 0, Rand(360), 0)		
	
	i\dist = EntityDistance(Collider, i\obj)
	
	;i\level = level
	
	Return i
	
End Function

Function RemoveItem(i.Items)
	Local n
	FreeEntity(i\obj) : i\obj = 0
	;If i\itemtemplate\invimg <> 0 Then FreeImage(i\InvIMG) : i\InvIMG = 0
	;If i\IMG <> 0 Then FreeImage(i\IMG) : i\IMG = 0
	
	For n% = 0 To MaxItemAmount - 1
		If Inventory(n) = i Then Inventory(n) = Null
	Next
	If SelectedItem = i Then
		Select SelectedItem\itemtemplate\tempname 
			Case "gasmask", "supergasmask"
				WearingGasMask = False
			Case "vest", "finevest"
				WearingVest = False
			Case "scp714"
				Wearing714 = False
		End Select
		
		SelectedItem = Null
	EndIf
	Delete i
End Function


Function UpdateItems()
	Local n, i.Items
	Local xtemp#, ytemp#, ztemp#
	
	ClosestItem = Null
	For i.Items = Each Items
		
		If (Not i\Picked) Then
			If i\disttimer =< 0 Then
				i\dist = EntityDistance(Collider, i\obj)
				i\disttimer = Rand(50,90)
			Else
				i\disttimer = Max(0, i\disttimer-FPSfactor)
			EndIf
			
			
			If i\dist < 1.2 Then
				If ClosestItem = Null Then
					If EntityInView(i\obj, Camera) Then ClosestItem = i
				Else
					If i\dist < EntityDistance(Collider, ClosestItem\obj) Then 
						If EntityInView(i\obj, Camera) Then ClosestItem = i
					EndIf
				End If
			EndIf						
			
			If i\dist < (HideDistance*0.5) Then
				If EntityCollided(i\obj, HIT_MAP) Then
					i\DropSpeed = 0
				Else
					i\DropSpeed = i\DropSpeed - 0.004 * FPSfactor * 0.1
					TranslateEntity i\obj, 0, i\DropSpeed * FPSfactor, 0
				EndIf					
				
				If EntityY(i\obj) < - 20.0 Then DebugLog "poistetaan: " + i\itemtemplate\name:RemoveItem(i)
			EndIf
		EndIf
		
	Next
	
	If ClosestItem <> Null Then
		;DrawHandIcon = True
		
		If MouseHit1 Then
			If ItemAmount < MaxItemAmount Then
				For n% = 0 To MaxItemAmount - 1
					If Inventory(n) = Null Then
						Select ClosestItem\itemtemplate\tempname
							Case "killbat"
								ShowEntity Light
								LightFlash = 1.0
								PlaySound(IntroSFX(11))
								Kill()
							Case "key6"
								Achievements(AchvOmni) = True
							Case "veryfinevest"
								Msg = "The vest is too heavy to pick up"
								MsgTimer = 70*6
								Exit
							Case "firstaid", "finefirstaid", "veryfinefirstaid", "firstaid2"
								ClosestItem\state = 0
							Case "navigator", "nav"
								If ClosestItem\itemtemplate\name = "S-NAV Navigator Ultimate" Then Achievements(AchvSNAV) = True
						End Select
						
						If ClosestItem\itemtemplate\sound <> 66 Then PlaySound(PickSFX(ClosestItem\itemtemplate\sound))
						ClosestItem\Picked = True
						
						ClosestItem\itemtemplate\found=True
						
						Inventory(n) = ClosestItem
						HideEntity(ClosestItem\obj)
						Exit
					EndIf
				Next
			Else
				Msg = "You can't carry any more items"
				MsgTimer = 70 * 5
			EndIf
		EndIf			
	End If
	
End Function


;~IDEal Editor Parameters:
;~F#9#1A#B0#C0#DD
;~C#Blitz3D