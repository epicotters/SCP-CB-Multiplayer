;This file was edited with BLIde ( http://www.blide.org )


Function SaveGame(file$)
	GameSaved = True

	Local x%, y%, i%, temp%

	CreateDir(file)
		
	Local f% = WriteFile(file + "save.txt")
	
	WriteString f, CurrentTime()
	WriteString f, CurrentDate()
	
	WriteInt f, PlayTime
	WriteFloat f, EntityX(Collider)
	WriteFloat f, EntityY(Collider)
	WriteFloat f, EntityZ(Collider)
	
	WriteByte f, PlayerLevel
	
	WriteString f, Str(AccessCode)
	
	WriteFloat f, EntityPitch(Collider)
	WriteFloat f, EntityYaw(Collider)
	
	WriteString f, VersionNumber
	
	WriteFloat f, BlinkTimer
	WriteFloat f, Stamina
	
	WriteFloat f, EyeSuper
	WriteFloat f, EyeStuck	
	WriteFloat f, EyeIrritation
	
	WriteFloat f, Injuries
	WriteFloat f, Bloodloss
	
	For i = 0 To 5
		WriteInt f, SCP1025state[i]
	Next
	
	WriteFloat f, Infect
	
	WriteByte f, SelectedMode
	
	WriteFloat f, Sanity
	
	WriteByte f, WearingGasMask
	WriteByte f, WearingVest
	WriteByte f, WearingHazmat
	WriteByte f, SuperMan
	WriteFloat f, SuperManTimer
	WriteByte f, LightsOn
	
	WriteString f, RandomSeed
	
	WriteFloat f, SecondaryLightOn
	WriteByte f, RemoteDoorOn
	WriteByte f, SoundTransmission
	WriteByte f, Contained106
	
	For i = 0 To MAXACHIEVEMENTS
		WriteByte f, Achievements(i)
	Next
	WriteInt f, RefinedItems
	
	WriteInt f, MapWidth
	WriteInt f, MapHeight
	For lvl = 0 To 0
		For x = 0 To MapWidth - 1
			For y = 0 To MapHeight - 1
				WriteInt f, MapTemp(lvl, x, y)
				WriteByte f, MapFound(lvl, x, y)
			Next
		Next
	Next
	
	
	WriteInt f, 113
	
	temp = 0
	For  n.NPCs = Each NPCs
		temp = temp +1
	Next	
	
	WriteInt f, temp
	For n.NPCs = Each NPCs
		WriteByte f, n\NPCtype
		WriteFloat f, EntityX(n\collider)
		WriteFloat f, EntityY(n\collider)
		WriteFloat f, EntityZ(n\collider)
		
		WriteFloat f, EntityPitch(n\collider)
		WriteFloat f, EntityYaw(n\collider)
		WriteFloat f, EntityRoll(n\collider)
		
		WriteFloat f, n\state
		WriteFloat f, n\state2
		WriteFloat f, n\state3
		WriteInt f, n\prevstate
		
		WriteByte f, n\idle
		WriteFloat f, n\lastDist
		WriteInt f, n\lastSeen
		
		WriteInt f, n\currspeed
		
		WriteFloat f, n\angle
		
		WriteFloat f, n\reload
		
		WriteInt f, n\ID
		If n\target <> Null Then
			WriteInt f, n\target\id		
		Else
			WriteInt f, 0
		EndIf
		
		WriteFloat f, n\enemyX
		WriteFloat f, n\enemyY
		WriteFloat f, n\enemyz
		
		WriteFloat f, AnimTime(n\obj)
	Next
	
	WriteFloat f, MTFtimer
	For i = 0 To 6
		If MTFrooms[0]<>Null Then 
			WriteString f, MTFrooms[0]\RoomTemplate\Name 
		Else 
			WriteString f,	"a"
		EndIf
		WriteInt f, MTFroomState[i]
	Next
	
	WriteInt f, 632
	
	temp = 0
	For r.Rooms = Each Rooms
		temp=temp+1
	Next	
	WriteInt f, temp	
	For r.Rooms = Each Rooms
		WriteInt f, r\roomtemplate\id
		WriteInt f, r\angle
		WriteFloat f, r\x
		WriteFloat f, r\y
		WriteFloat f, r\z
		
		WriteByte f, r\found
		
		WriteInt f, r\level
		
		If PlayerRoom = r Then 
			WriteByte f, 1
		Else 
			WriteByte f, 0
		EndIf
		
		For i = 0 To 11
			If r\npc[0]=Null Then
				WriteInt f, 0
			Else
				WriteInt f, r\npc[0]\id
			EndIf
		Next
	Next
	
	WriteInt f, 954
	
	temp = 0
	For do.Doors = Each Doors
		temp = temp+1	
	Next	
	WriteInt f, temp	
	For do.Doors = Each Doors
		WriteFloat f, EntityX(do\frameobj,True)
		WriteFloat f, EntityZ(do\frameobj,True)
		WriteByte f, do\open
		WriteFloat f, do\openstate
		WriteByte f, do\locked
		WriteByte f, do\AutoClose
		
		WriteFloat f, EntityX(do\obj, True)
		WriteFloat f, EntityZ(do\obj, True)
		
		If do\obj2 <> 0 Then
			WriteFloat f, EntityX(do\obj2, True)
			WriteFloat f, EntityZ(do\obj2, True)
		Else
			WriteFloat f, 0.0
			WriteFloat f, 0.0
		End If
		
		WriteFloat f, do\timer
		WriteFloat f, do\timerstate	
	Next
	
	WriteInt f, 1845
	DebugLog 1845
	
	Local d.Decals
	temp = 0
	For d.Decals = Each Decals
		temp = temp+1
	Next	
	WriteInt f, temp
	For d.Decals = Each Decals
		WriteInt f, d\ID
		
		;DebugLog d.id + ": " + d\obj

		WriteFloat f, d\x
		WriteFloat f, d\y
		WriteFloat f, d\z
		
		WriteFloat f, d\pitch
		WriteFloat f, d\yaw
		WriteFloat f, d\roll
		
		WriteByte f, d\blendmode
		WriteInt f, d\fx
		
		DebugLog "eeeeeeeeee"
		
		WriteFloat f, d\Size
		WriteFloat f, d\Alpha
		WriteFloat f, d\AlphaChange
		WriteFloat f, d\Timer
		WriteFloat f, d\lifetime
	Next
	
	temp = 0
	For e.events = Each Events
		temp=temp+1
	Next	
	WriteInt f, temp
	For e.events = Each Events
		WriteString f, e\eventName		
		WriteFloat f, e\eventstate
		WriteFloat f, e\eventstate2	
		WriteFloat f, e\eventstate3	
		WriteFloat f, EntityX(e\room\obj)
		WriteFloat f, EntityZ(e\room\obj)
	Next
	
	temp = 0
	For it.items = Each Items	
		temp=temp+1
	Next
	WriteInt f, temp
	For it.items = Each Items
		WriteString f, it\itemtemplate\name
		WriteString f, it\itemtemplate\tempName
		
		WriteFloat f, EntityX(it\obj, True)
		WriteFloat f, EntityY(it\obj, True)
		WriteFloat f, EntityZ(it\obj, True)
		
		WriteFloat f, EntityPitch(it\obj)
		WriteFloat f, EntityYaw(it\obj)
		
		WriteFloat f, it\state
		WriteByte f, it\Picked
		
		If SelectedItem = it Then WriteByte f, 1 Else WriteByte f, 0
		Local ItemFound% = False
		For i = 0 To MaxItemAmount - 1
			If Inventory(i) = it Then ItemFound = True : Exit
		Next
		If ItemFound Then WriteByte f, i Else WriteByte f, 66
	Next
	
	For itt.itemtemplates = Each ItemTemplates
		WriteByte f, itt\found
	Next	
	
	WriteInt f, 994
	DebugLog 994
		
	CloseFile f

End Function

Function LoadGame(file$)
	GameSaved = True

	Local x#, y#, z#, i%, temp%, strtemp$, r.Rooms, id%
	Local f% = ReadFile(file + "save.txt")
	
	strtemp = ReadString(f)
	strtemp = ReadString(f)

	PlayTime = ReadInt(f)
	
	x = ReadFloat(f)
	y = ReadFloat(f)
	z = ReadFloat(f)	
	PositionEntity(Collider, x, y+0.05, z)
	ResetEntity(Collider)
	
	PlayerLevel = ReadByte(f)
	
	AccessCode = Int(ReadString(f))

	x = ReadFloat(f)
	y = ReadFloat(f)
	RotateEntity(Collider, x, y, 0, 0)
	
	strtemp = ReadString(f)
	If strtemp <> VersionNumber Then RuntimeError("The save files of v"+stremp+" aren't compatible with SCP - Containment Breach v"+VersionNumber+".")
	
	BlinkTimer = ReadFloat(f)
	Stamina = ReadFloat(f)
	
	EyeSuper = ReadFloat(f)
	EyeStuck	= ReadFloat(f)
	EyeIrritation= ReadFloat(f)
	
	Injuries = ReadFloat(f)
	Bloodloss = ReadFloat(f)
	
	For i = 0 To 5
		SCP1025state[i]=ReadInt(f)
	Next
	
	Infect=ReadFloat(f)
	
	SelectedMode = ReadByte(f)
	
	Sanity = ReadFloat(f)
		
	WearingGasMask = ReadByte(f)
	WearingVest = ReadByte(f)	
	WearingHazmat = ReadByte(f)
	SuperMan = ReadByte(f)
	SuperManTimer = ReadFloat(f)
	LightsOn = ReadByte(f)
	
	RandomSeed = ReadString(f)
	
	SecondaryLightOn = ReadFloat(f)
	RemoteDoorOn = ReadByte(f)
	SoundTransmission = ReadByte(f)	
	Contained106 = ReadByte(f)	
	
	For i = 0 To MAXACHIEVEMENTS
		Achievements(i)=ReadByte(f)
	Next
	RefinedItems = ReadInt(f)
	
	MapWidth = ReadInt(f)
	MapHeight = ReadInt(f)
	For lvl = 0 To 0
		For x = 0 To MapWidth - 1
			For y = 0 To MapHeight - 1
				MapTemp(lvl, x, y) = ReadInt(f)
				MapFound(lvl, x, y) = ReadByte(f)
			Next
		Next
	Next
	
	
	If ReadInt(f) <> 113 Then RuntimeError("Couldn't load the game, save file corrupted (error 2.5)")
	
	temp = ReadInt(f)
	For i = 1 To temp
		Local NPCtype% = ReadByte(f)
		x = ReadFloat(f)
		y = ReadFloat(f)
		z = ReadFloat(f)
		
		n.NPCs = CreateNPC(NPCtype, x, y, z)
		Select NPCtype
			Case NPCtype173
				Curr173 = n
			Case NPCtypeOldMan
				Curr106 = n
			Case NPCtype096
				Curr096 = n
		End Select
		
		x = ReadFloat(f)
		y = ReadFloat(f)
		z = ReadFloat(f)
		RotateEntity(n\collider, x, y, z)
		
		n\state = ReadFloat(f)
		n\state2 = ReadFloat(f)	
		n\state3 = ReadFloat(f)			
		n\prevstate = ReadInt(f)
		
		n\idle = ReadByte(f)
		n\lastDist = ReadFloat(f)
		n\lastSeen = ReadInt(f)
		
		n\currspeed = ReadInt(f)
		n\angle = ReadFloat(f)
		n\reload = ReadFloat(f)
		
		n\id = ReadInt(f)
		n\targetid = ReadInt(f)
		
		n\enemyX = ReadFloat(f)
		n\enemyY = ReadFloat(f)
		n\enemyz = ReadFloat(f)
		
		Local frame# = ReadFloat(f)
		Select NPCtype
			Case NPCtypeOldMan, NPCtypeD, NPCtype096, NPCtypeMTF, NPCtypeGuard, NPCtype049, NPCtypeZombie
				SetAnimTime(n\obj, frame)
		End Select		
		
	Next
	
	For n.npcs = Each NPCs
		If n\targetid <> 0 Then
			For n2.npcs = Each NPCs
				If n2<>n Then
					If n2\id = n\targetid Then n\target = n2
				EndIf
			Next
		EndIf
	Next
	
	MTFtimer = ReadFloat(f)
	For i = 0 To 6
		strtemp =  ReadString(f)
		If strtemp <> "a" Then
			For r.Rooms = Each Rooms
				If r\RoomTemplate\Name = strtemp Then
					MTFrooms[i]=r
				EndIf
			Next
		EndIf
		MTFroomState[i]=ReadInt(f)
	Next
	
	If ReadInt(f) <> 632 Then RuntimeError("Couldn't load the game, save file corrupted (error 1)")
			
	temp = ReadInt(f)
	For i = 1 To temp
		Local roomtemplateID% = ReadInt(f)
		Local angle% = ReadInt(f)
		x = ReadFloat(f)
		y = ReadFloat(f)
		z = ReadFloat(f)
		
		found = ReadByte(f)
		
		level = ReadInt(f)
		
		temp2 = ReadByte(f)		
		
		For rt.roomtemplates = Each RoomTemplates
			If rt\id = roomtemplateID Then
				r.Rooms = CreateRoom(level, rt\shape, x, y, z, rt\name)
				TurnEntity(r\obj, 0, angle, 0)
				r\angle = angle
				r\found = found
				Exit
			End If
		Next
		
		If temp2 = 1 Then PlayerRoom = r.Rooms
		
		For x = 0 To 11
			id = ReadInt(f)
			If id > 0 Then
				For n.npcs = Each NPCs
					If n\id = id Then r\NPC[x]=n : Exit
				Next
			EndIf
		Next
	Next
	
	If ReadInt(f) <> 954 Then RuntimeError("Couldn't load the game, save file may be corrupted (error 2)")
	
	Local spacing# = 8.0
	For lvl = 0 To 0
		For y = MapHeight - 2 To 1 Step - 1
			For x = 1 To MapWidth - 2
				If MapTemp(lvl, x, y) > 0 Then
	
					If (Floor((x + y) / 2.0) = Ceil((x + y) / 2.0)) Then
						If MapTemp(lvl, x + 1, y) Then
							CreateDoor(lvl, x * spacing + spacing / 2.0, 0, y * spacing, 90, Null, Max(Rand(-3, 1), 0))
						EndIf
						
						If MapTemp(lvl, x - 1, y) Then
							CreateDoor(lvl, x * spacing - spacing / 2.0, 0, y * spacing, 90, Null, Max(Rand(-3, 1), 0))
						EndIf
						
						If MapTemp(lvl, x, y + 1) Then
							CreateDoor(lvl, x * spacing, 0, y * spacing + spacing / 2.0, 0, Null, Max(Rand(-3, 1), 0))
						EndIf
						
						If MapTemp(lvl, x, y - 1)Then
							CreateDoor(lvl, x * spacing, 0, y * spacing - spacing / 2.0, 0, Null, Max(Rand(-3, 1), 0))
						EndIf
					End If
				EndIf
			Next
		Next
	Next
	
	temp = ReadInt (f)
	;DebugLog temp + ", " + CountList(DoorList)
	For i = 1 To temp
		x = ReadFloat(f)
		z = ReadFloat(f)
		
		Local open% = ReadByte(f)
		Local openstate# = ReadFloat(f)
		Local locked% = ReadByte(f)
		Local autoclose% = ReadByte(f)
		
		Local objX# = ReadFloat(f)
		Local objZ# = ReadFloat(f)
		
		Local obj2X# = ReadFloat(f)
		Local obj2Z# = ReadFloat(f)
				
		Local timer% = ReadFloat(f)
		Local timerstate# = ReadFloat(f)
			
		For do.Doors = Each Doors
			If EntityX(do\frameobj,True) = x And EntityZ(do\frameobj,True) = z Then
				do\open = open
				do\openstate = openstate
				do\locked = locked
				do\AutoClose = autoclose
				do\timer = timer
				do\timerstate = timerstate
				
				PositionEntity(do\obj, objX, EntityY(do\obj), objZ, True)
				If do\obj2 <> 0 Then PositionEntity(do\obj2, obj2X, EntityY(do\obj2), obj2Z, True)
				Exit
			End If
		Next		
	Next
	
	InitWayPoints()
	
	If ReadInt(f) <> 1845 Then RuntimeError("Couldn't load the game, save file corrupted (error 3)")
	
	temp = ReadInt(f)
	For i = 1 To temp
		id% = ReadInt(f)
		x = ReadFloat(f)
		y = ReadFloat(f)
		z = ReadFloat(f)
		Local pitch# = ReadFloat(f)
		Local yaw# = ReadFloat(f)
		Local roll# = ReadFloat(f)
		Local d.Decals = CreateDecal(id, x, y, z, pitch, yaw, roll)
		d\blendmode = ReadByte (f)
		d\fx = ReadInt(f)
		
		d\Size = ReadFloat(f)
		d\Alpha = ReadFloat(f)
		d\AlphaChange = ReadFloat(f)
		d\Timer = ReadFloat(f)
		d\lifetime = ReadFloat(f)
		
		ScaleSprite(d\obj, d\Size, d\Size)
		EntityBlend d\obj, d\blendmode
		EntityFX d\obj, d\fx
	Next
	
	temp = ReadInt(f)
	For i = 1 To temp
		Local e.Events = New Events
		e\EventName = ReadString(f)
		
		e\EventState =ReadFloat(f)
		e\EventState2 =ReadFloat(f)		
		e\EventState3 =ReadFloat(f)
		x = ReadFloat(f)
		z = ReadFloat(f)
		For  r.Rooms = Each Rooms
			If EntityX(r\obj) = x And EntityZ(r\obj) = z Then
				e\room = r
				Exit
			EndIf
		Next	
	Next
	
	Local it.Items
	For it.Items = Each Items
		RemoveItem(it)
	Next
	
	temp = ReadInt(f)
	For i = 1 To temp
		Local Name$ = ReadString(f)
		Local tempName$ = ReadString(f)
		
		x = ReadFloat(f)
		y = ReadFloat(f)
		z = ReadFloat(f)		
			
		it.Items = CreateItem(Name, tempName, x, y, z)
		
		EntityType it\obj, HIT_ITEM
		
		;DebugLog Name + ", " + tempName + ", " + x + ", " + y + ", " + z + ", " + scale
		
		x = ReadFloat(f)
		y = ReadFloat(f)
		RotateEntity(it\obj, x, y, 0)
		
		it\state = ReadFloat(f)
		it\Picked = ReadByte(f)
		If it\Picked Then HideEntity(it\obj)
		
		nt = ReadByte(f)
		If nt = True Then SelectedItem = it
		
		nt = ReadByte(f)
		If nt < 66 Then Inventory(nt) = it
	Next	
	
	For itt.ItemTemplates = Each ItemTemplates
		itt\found = ReadByte(f)
	Next
	
	For do.Doors = Each Doors
		If do\room <> Null Then
			dist# = 20.0
			Local closestroom.Rooms
			For r.Rooms = Each Rooms
				dist2# = EntityDistance(r\obj, do\obj)
				If dist2 < dist Then
					dist = dist2
					closestroom = r.Rooms
				EndIf
			Next
			do\room = closestroom
		EndIf
	Next
		
	If ReadInt(f) <> 994 Then RuntimeError("Couldn't load the game, save file corrupted (error 4)")
	
	CloseFile f
		
End Function

Function LoadGameQuick(file$)
	GameSaved = True
	
	Local x#, y#, z#, i%, temp%, strtemp$, id%
	Local f% = ReadFile(file + "save.txt")
	
	strtemp = ReadString(f)
	strtemp = ReadString(f)
	
	DropSpeed = 0
	Shake = 0
	CurrSpeed = 0
	
	HeartBeatVolume = 0
	
	CameraShake = 0
	Shake = 0
	LightFlash = 0
	BlurTimer = 0
	
	KillTimer = 0
	FallTimer = 0
	MenuOpen = False
	
	GodMode = 0
	NoClip = 0
	
	PlayTime = ReadInt(f)
	
	HideEntity Head
	ShowEntity Collider
	
	x = ReadFloat(f)
	y = ReadFloat(f)
	z = ReadFloat(f)	
	PositionEntity(Collider, x, y+0.05, z)
	ResetEntity(Collider)
	
	PlayerLevel = ReadByte(f)
	
	AccessCode = Int(ReadString(f))
	
	x = ReadFloat(f)
	y = ReadFloat(f)
	RotateEntity(Collider, x, y, 0, 0)
	
	strtemp = ReadString(f)
	;If strtemp <> VersionNumber Then RuntimeError("The save files of v"+stremp+" aren't compatible with SCP - Containment Breach v"+VersionNumber+".")
	
	BlinkTimer = ReadFloat(f)
	Stamina = ReadFloat(f)
	
	EyeSuper = ReadFloat(f)
	EyeStuck	= ReadFloat(f)
	EyeIrritation= ReadFloat(f)
	
	Injuries = ReadFloat(f)
	Bloodloss = ReadFloat(f)	
	
	For i = 0 To 5
		SCP1025state[i]=ReadInt(f)
	Next
	
	Infect=ReadFloat(f)
	
	SelectedMode = ReadByte(f)
	
	Sanity = ReadFloat(f)
	
	WearingGasMask = ReadByte(f)
	WearingVest = ReadByte(f)	
	WearingHazmat = ReadByte(f)
	SuperMan = ReadByte(f)
	SuperManTimer = ReadFloat(f)
	LightsOn = ReadByte(f)
	
	RandomSeed = ReadString(f)
	
	SecondaryLightOn = ReadFloat(f)
	RemoteDoorOn = ReadByte(f)
	SoundTransmission = ReadByte(f)	
	Contained106 = ReadByte(f)	
	
	For i = 0 To MAXACHIEVEMENTS
		Achievements(i)=ReadByte(f)
	Next
	RefinedItems = ReadInt(f)
	
	MapWidth = ReadInt(f)
	MapHeight = ReadInt(f)
	DebugLog MapWidth
	DebugLog MapHeight
	For lvl = 0 To 0
		For x = 0 To MapWidth - 1
			For y = 0 To MapHeight - 1
				MapTemp(lvl, x, y) = ReadInt(f)
				MapFound(lvl, x, y) = ReadByte(f)
			Next
		Next
	Next
	
	If ReadInt(f) <> 113 Then RuntimeError("Couldn't load the game, save file corrupted (error 2.5)")
	
	For n.npcs = Each NPCs
		RemoveNPC(n)
	Next
	
	temp = ReadInt(f)
	For i = 1 To temp
		Local NPCtype% = ReadByte(f)
		x = ReadFloat(f)
		y = ReadFloat(f)
		z = ReadFloat(f)
		
		n.NPCs = CreateNPC(NPCtype, x, y, z)
		Select NPCtype
			Case NPCtype173
				Curr173 = n
			Case NPCtypeOldMan
				Curr106 = n
		End Select
		
		x = ReadFloat(f)
		y = ReadFloat(f)
		z = ReadFloat(f)
		RotateEntity(n\collider, x, y, z)
		
		n\state = ReadFloat(f)
		n\state2 = ReadFloat(f)	
		n\state3 = ReadFloat(f)			
		n\prevstate = ReadInt(f)
		
		n\idle = ReadByte(f)
		n\lastDist = ReadFloat(f)
		n\lastSeen = ReadInt(f)
		
		n\currspeed = ReadInt(f)
		n\angle = ReadFloat(f)
		n\reload = ReadFloat(f)
		
		n\id = ReadInt(f)
		n\targetid = ReadInt(f)
		
		n\enemyX = ReadFloat(f)
		n\enemyY = ReadFloat(f)
		n\enemyz = ReadFloat(f)
		
		Local frame# = ReadFloat(f)
		Select NPCtype
			Case NPCtypeOldMan, NPCtypeD, NPCtype096, NPCtypeMTF, NPCtypeGuard, NPCtype049, NPCtypeZombie
				SetAnimTime(n\obj, frame)
		End Select		
		
	Next
	
	For n.npcs = Each NPCs
		If n\targetid <> 0 Then
			For n2.npcs = Each NPCs
				If n2<>n Then
					If n2\id = n\targetid Then n\target = n2
				EndIf
			Next
		EndIf
	Next
	
	MTFtimer = ReadFloat(f)
	For i = 0 To 6
		strtemp =  ReadString(f)
		If strtemp <> "a" Then
			For r.Rooms = Each Rooms
				If r\RoomTemplate\Name = strtemp Then
					MTFrooms[i]=r
				EndIf
			Next
		EndIf
		MTFroomState[i]=ReadInt(f)
	Next
	
	If ReadInt(f) <> 632 Then RuntimeError("Couldn't load the game, save file corrupted (error 1)")
	
	temp = ReadInt(f)
	For i = 1 To temp
		Local roomtemplateID% = ReadInt(f)
		Local angle% = ReadInt(f)
		x = ReadFloat(f)
		y = ReadFloat(f)
		z = ReadFloat(f)
		
		found = ReadByte(f)
		
		level = ReadInt(f)
		
		temp2 = ReadByte(f)	
		
		For x = 0 To 11
			id = ReadInt(f)
		Next
		;If temp2 = 1 Then PlayerRoom = r.Rooms
	Next
	
	InitWayPoints()
	
	If ReadInt(f) <> 954 Then RuntimeError("Couldn't load the game, save file may be corrupted (error 2)")
	
	temp = ReadInt (f)
	;DebugLog temp + ", " + CountList(DoorList)
	For i = 1 To temp
		x = ReadFloat(f)
		z = ReadFloat(f)
		
		Local open% = ReadByte(f)
		Local openstate# = ReadFloat(f)
		Local locked% = ReadByte(f)
		Local autoclose% = ReadByte(f)
		
		Local objX# = ReadFloat(f)
		Local objZ# = ReadFloat(f)
		
		Local obj2X# = ReadFloat(f)
		Local obj2Z# = ReadFloat(f)
		
		Local timer% = ReadFloat(f)
		Local timerstate# = ReadFloat(f)
		
		For do.Doors = Each Doors
			If EntityX(do\frameobj,True) = x And EntityZ(do\frameobj,True) = z Then
				do\open = open
				do\openstate = openstate
				do\locked = locked
				do\AutoClose = autoclose
				do\timer = timer
				do\timerstate = timerstate
				
				PositionEntity(do\obj, objX, EntityY(do\obj), objZ, True)
				If do\obj2 <> 0 Then PositionEntity(do\obj2, obj2X, EntityY(do\obj2), obj2Z, True)
				
				Exit
			End If
		Next		
	Next
	
	If ReadInt(f) <> 1845 Then RuntimeError("Couldn't load the game, save file corrupted (error 3)")
	
	Local d.Decals
	For d.Decals = Each Decals
		FreeEntity d\obj
		Delete d
	Next
	
	temp = ReadInt(f)
	For i = 1 To temp
		id% = ReadInt(f)
		x = ReadFloat(f)
		y = ReadFloat(f)
		z = ReadFloat(f)
		Local pitch# = ReadFloat(f)
		Local yaw# = ReadFloat(f)
		Local roll# = ReadFloat(f)
		d.Decals = CreateDecal(id, x, y, z, pitch, yaw, roll)
		d\blendmode = ReadByte (f)
		d\fx = ReadInt(f)
		
		d\Size = ReadFloat(f)
		d\Alpha = ReadFloat(f)
		d\AlphaChange = ReadFloat(f)
		d\Timer = ReadFloat(f)
		d\lifetime = ReadFloat(f)
		
		ScaleSprite(d\obj, d\Size, d\Size)
		EntityBlend d\obj, d\blendmode
		EntityFX d\obj, d\fx
	Next
	
	Local e.Events
	For e.Events = Each Events
		If e\Sound <> 0 Then FreeSound e\Sound
		Delete e
	Next
	
	temp = ReadInt(f)
	For i = 1 To temp
		e.Events = New Events
		e\EventName = ReadString(f)
		
		e\EventState = ReadFloat(f)
		e\EventState2 = ReadFloat(f)
		e\EventState3 = ReadFloat(f)		
		x = ReadFloat(f)
		z = ReadFloat(f)
		For r.Rooms = Each Rooms
			If EntityX(r\obj) = x And EntityZ(r\obj) = z Then
				e\room = r
				Exit
			EndIf
		Next	
	Next
	
	Local it.Items
	For it.Items = Each Items
		RemoveItem(it)
	Next
	
	temp = ReadInt(f)
	For i = 1 To temp
		Local Name$ = ReadString(f)
		Local tempName$ = ReadString(f)
		
		x = ReadFloat(f)
		y = ReadFloat(f)
		z = ReadFloat(f)		
		
		it.Items = CreateItem(Name, tempName, x, y, z)
		
		EntityType it\obj, HIT_ITEM
		
		;DebugLog Name + ", " + tempName + ", " + x + ", " + y + ", " + z + ", " + scale
		
		x = ReadFloat(f)
		y = ReadFloat(f)
		RotateEntity(it\obj, x, y, 0)
		
		it\state = ReadFloat(f)
		it\Picked = ReadByte(f)
		If it\Picked Then HideEntity(it\obj)
		
		nt = ReadByte(f)
		If nt = True Then SelectedItem = it
		
		nt = ReadByte(f)
		If nt < 66 Then Inventory(nt) = it
	Next	
	
	For itt.ItemTemplates = Each ItemTemplates
		itt\found = ReadByte(f)
	Next
	
	For do.Doors = Each Doors
		If do\room <> Null Then
			dist# = 20.0
			Local closestroom.Rooms
			For r.Rooms = Each Rooms
				dist2# = EntityDistance(r\obj, do\obj)
				If dist2 < dist Then
					dist = dist2
					closestroom = r.Rooms
				EndIf
			Next
			do\room = closestroom
		EndIf
	Next
	
	If ReadInt(f) <> 994 Then RuntimeError("Couldn't load the game, save file corrupted (error 4)")
	
	If 0 Then 
		closestroom = Null
		dist = 30
		For r.Rooms = Each Rooms
			dist2# = EntityDistance(r\obj, Collider)
			If dist2 < dist Then
				dist = dist2
				closestroom = r
			EndIf
		Next
		
		If closestroom<>Null Then PlayerRoom = closestroom
	EndIf
	
	CloseFile f
	
End Function

Function LoadSaveGames()
	SaveGameAmount = 0
	myDir=ReadDir(SavePath) 
	Repeat 
		file$=NextFile$(myDir) 
		If file$="" Then Exit 
		If FileType(SavePath+"\"+file$) = 2 Then 
			If file <> "." And file <> ".." Then SaveGameAmount=SaveGameAmount+1
		End If 
	Forever 
	CloseDir myDir 
	
	Dim SaveGames$(SaveGameAmount+1) 
	
	myDir=ReadDir(SavePath) 
	i = 0
	Repeat 
		file$=NextFile$(myDir) 
		If file$="" Then Exit 
		If FileType(SavePath+"\"+file$) = 2 Then 
			If file <> "." And file <> ".." Then 
				SaveGames(i) = file
				i=i+1
			EndIf
		End If 
	Forever 
	CloseDir myDir 
	
	Dim SaveGameTime$(SaveGameAmount + 1)
	Dim SaveGameDate$(SaveGameAmount + 1)
	For i = 1 To SaveGameAmount
		DebugLog (SavePath + SaveGames(i - 1) + "\save.txt")
		Local f% = ReadFile(SavePath + SaveGames(i - 1) + "\save.txt")
		SaveGameTime(i - 1) = ReadString(f)
		SaveGameDate(i - 1) = ReadString(f)
		CloseFile f
	Next
End Function

;~IDEal Editor Parameters:
;~F#3FD
;~C#Blitz3D