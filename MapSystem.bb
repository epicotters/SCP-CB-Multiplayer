

Type Materials
	Field name$
	Field Diff
	Field Bump
	;Field Spec
End Type

Function LoadMaterials(file$)
	If Not BumpEnabled Then Return
	
	Local TemporaryString$, Temp%, i%, n%
	Local mat.Materials = Null
	Local StrTemp$ = ""
	
	Local f = OpenFile(file)
	
	While Not Eof(f)
		TemporaryString = Trim(ReadLine(f))
		If Left(TemporaryString,1) = "[" Then
			TemporaryString = Mid(TemporaryString, 2, Len(TemporaryString) - 2)
			
			mat.Materials = New Materials
			
			mat\name = Lower(TemporaryString)
			;mat\Diff =  LoadTexture(GetINIString(file, TemporaryString, "diff"))
			mat\Bump =  LoadTexture(GetINIString(file, TemporaryString, "bump"))
			TextureBlend mat\Bump, FE_BUMP
			
			;mat\Spec =  LoadTexture(GetINIString(file, TemporaryString, "spec"))
		EndIf
	Wend
	
	CloseFile f
	
End Function

Function LoadWorld(file$, rt.RoomTemplates)
	Local map=LoadAnimMesh(file)
	If Not map Then Return
	Local world=CreatePivot()
	Local meshes=CreatePivot(world)
	Local renderbrushes=CreateMesh(world)
	Local collisionbrushes=CreatePivot(world)
	Local pointentities=CreatePivot(world)
	Local solidentities=CreatePivot(world)
	EntityType collisionbrushes,HIT_MAP
	
	DebugLog "loadworld: "+file
	
	For c=1 To CountChildren(map)
		
		Local node=GetChild(map,c)	
		Local classname$=Lower(KeyValue(node,"classname"))
		;DebugLog "Loading "+Chr(34)+classname+Chr(34)+"..."
		Select classname
				
			;===============================================================================
			;Map Geometry
			;===============================================================================
				
			Case "mesh"
				EntityParent node,meshes
				EntityType node,HIT_MAP
				EntityPickMode node, 2
				
				c=c-1
				;EntityType node,HIT_MAP
				
			Case "brush"
				RotateMesh node,EntityPitch(node),EntityYaw(node),EntityRoll(node)
				PositionMesh node,EntityX(node),EntityY(node),EntityZ(node)
				
				AddMesh node,renderbrushes	
				
				EntityAlpha node,0
				EntityType node,HIT_MAP
				EntityAlpha node,0
				EntityParent node,collisionbrushes
				EntityPickMode node, 2
				
				c=c-1
				
			;===============================================================================
			;Solid Entities
			;===============================================================================
			Case "item"
				;name$ = KeyValue(node,"name","")
				;tempname$ = KeyValue(node,"tempname","")				
				;CreateItem(name,tempname,EntityX(node)*RoomScale,EntityY(node)*RoomScale,EntityZ(node)*RoomScale)
			Case "screen"
				
				x# = EntityX(node)*RoomScale
				y# = EntityY(node)*RoomScale
				z# = EntityZ(node)*RoomScale
				
				If x<>0 Or y<>0 Or z<>0 Then 
					ts.tempscreens = New TempScreens	
					ts\x = x
					ts\y = y
					ts\z = z
					ts\imgpath = KeyValue(node,"imgpath","")
					ts\roomtemplate = rt
				EndIf
				
			Case "waypoint"
				x# = EntityX(node)*RoomScale
				y# = EntityY(node)*RoomScale
				z# = EntityZ(node)*RoomScale				
				w.tempwaypoints = New TempWayPoints
				w\roomtemplate = rt
				w\x = x
				w\y = y
				w\z = z
				;EntityParent (w\obj, collisionbrushes)
				
			Case "light"
				x# = EntityX(node)*RoomScale
				y# = EntityY(node)*RoomScale
				z# = EntityZ(node)*RoomScale
				
				If x<>0 Or y<>0 Or z<>0 Then 
					range# = Float(KeyValue(node,"range","1"))/1000.0
					lcolor$=KeyValue(node,"color","255 255 255")
					intensity# = Min(Float(KeyValue(node,"intensity","1.0"))*0.8,1.0)
					r=Int(Piece(lcolor,1," "))*intensity
					g=Int(Piece(lcolor,2," "))*intensity
					b=Int(Piece(lcolor,3," "))*intensity
					
					AddTempLight(rt, x,y,z, 2, range, r,g,b)
				EndIf
			Case "spotlight"	
				x# = EntityX(node)*RoomScale
				y# = EntityY(node)*RoomScale
				z# = EntityZ(node)*RoomScale
				If x<>0 Or y<>0 Or z<>0 Then 
					range# = Float(KeyValue(node,"range","1"))/700.0
					lcolor$=KeyValue(node,"color","255 255 255")
					intensity# = Min(Float(KeyValue(node,"intensity","1.0"))*0.8,1.0)
					r=Int(Piece(lcolor,1," "))*intensity
					g=Int(Piece(lcolor,2," "))*intensity
					b=Int(Piece(lcolor,3," "))*intensity
					
					lt.lighttemplates = AddTempLight(rt, x,y,z, 3, range, r,g,b)
					angles$=KeyValue(node,"angles","0 0 0")
					pitch#=Piece(angles,1," ")
					yaw#=Piece(angles,2," ")
					lt\pitch = pitch
					lt\yaw = yaw
					
					lt\innerconeangle = Int(KeyValue(node,"innerconeangle",""))
					lt\outerconeangle = Int(KeyValue(node,"outerconeangle",""))	
				EndIf
			Case "soundemitter"
				DebugLog "soundemitter*******************************************************************"
				For i = 0 To 3
					If rt\tempsoundemitter[i]=0 Then
						rt\tempsoundemitterx[i]=EntityX(node)*RoomScale
						rt\tempsoundemittery[i]=EntityY(node)*RoomScale
						rt\tempsoundemitterz[i]=EntityZ(node)*RoomScale
						rt\tempsoundemitter[i]=Int(KeyValue(node,"sound","0"))
						
						rt\tempsoundemitterrange[i]=Float(KeyValue(node,"range","1"))
						Exit
					EndIf
				Next
				
			;Invisible collision brush
			Case "field_hit"
				EntityParent node,collisionbrushes
				EntityType node,HIT_MAP
				EntityAlpha node,0
				c=c-1
			;Case "terrain"
				
				;fillroomilla erillisenä meshi huoneeseen
				
				;EntityParent node,meshes
				;EntityType node,HIT_MAP
				;EntityPickMode node, 2
				;c = c - 1
			;===============================================================================
			;Point Entities
			;===============================================================================
				
			;Camera start position point entity
			Case "playerstart"
				angles$=KeyValue(node,"angles","0 0 0")
				pitch#=Piece(angles,1," ")
				yaw#=Piece(angles,2," ")
				roll#=Piece(angles,3," ")
				If cam Then
					PositionEntity cam,EntityX(node),EntityY(node),EntityZ(node)
					RotateEntity cam,pitch,yaw,roll
				EndIf
				
		End Select
	Next
	
	If BumpEnabled Then 
		
		For i = 1 To CountSurfaces(renderbrushes)
			sf = GetSurface(renderbrushes,i)
			b = GetSurfaceBrush( sf )
			t = GetBrushTexture(b, 1)
			texname$ =  StripPath(TextureName(t))
			
			For mat.materials = Each Materials
				If texname = mat\name Then
					
					DebugLog mat\name
					t1 = GetBrushTexture(b,0)
					;t2 = GetBrushTexture(b,1)
					
					BrushTexture b, t1, 0, 0 ;light map
					BrushTexture b, mat\bump, 0, 1 ;bump
					BrushTexture b, t, 0, 2 ;diff
					
					;TextureFilter
					
					;BrushAlpha b1, 1.0
					;BrushTexture b1, t3, 0, 2				
					; ... and other textures
					PaintSurface sf,b
					
					If StripPath(TextureName(t1)) <> "" Then FreeTexture t1
					;If StripPath(TextureName(t2)) <> "" Then FreeTexture t2	
					
					;If t1<>0 Then FreeTexture t1
					;If t2 <> 0 Then FreeTexture t2						
					
					Exit
				EndIf 
			Next
			
			FreeTexture t
			FreeBrush b
			
		Next
		
	EndIf
	
	EntityFX renderbrushes, 1
	
	FreeEntity map
	
	Return world	
	
	
End Function




Function StripPath$(file$) 
	
	If Len(file$)>0 
		
		For i=Len(file$) To 1 Step -1 
			
			mi$=Mid$(file$,i,1) 
			If mi$="\" Or mi$="/" Then Return name$ Else name$=mi$+name$ 
			
		Next 
		
	EndIf 
	
	Return name$ 
	
End Function 

Function Piece$(s$,entry,char$=" ")
	While Instr(s,char+char)
		s=Replace(s,char+char,char)
	Wend
	For n=1 To entry-1
		p=Instr(s,char)
		s=Right(s,Len(s)-p)
	Next
	p=Instr(s,char)
	If p<1
		a$=s
	Else
		a=Left(s,p-1)
	EndIf
	Return a
End Function

Function KeyValue$(entity,key$,defaultvalue$="")
	properties$=EntityName(entity)
	properties$=Replace(properties$,Chr(13),"")
	key$=Lower(key)
	Repeat
		p=Instr(properties,Chr(10))
		If p Then test$=(Left(properties,p-1)) Else test=properties
		testkey$=Piece(test,1,"=")
		testkey=Trim(testkey)
		testkey=Replace(testkey,Chr(34),"")
		testkey=Lower(testkey)
		If testkey=key Then
			value$=Piece(test,2,"=")
			value$=Trim(value$)
			value$=Replace(value$,Chr(34),"")
			Return value
		EndIf
		If Not p Then Return defaultvalue$
		properties=Right(properties,Len(properties)-p)
	Forever 
End Function



Const ROOM1% = 1, ROOM2% = 2, ROOM2C% = 3, ROOM3% = 4, ROOM4% = 5

Global RoomTempID%
Type RoomTemplates
	Field obj%, id%
	Field objPath$
	
	Field zone%[5]
	
	;Field ambience%
	
	Field TempSoundEmitter%[4]
	Field TempSoundEmitterX#[4]
	Field TempSoundEmitterY#[4]
	Field TempSoundEmitterZ#[4]
	Field TempSoundEmitterRange#[4]
	
	Field Shape%, Name$
	Field Difficulty%, Commonness%
	Field DisableDecals%
	Field StepSound%
End Type 	

Type LightTemplates
	Field roomtemplate.RoomTemplates
	Field ltype%
	Field x#, y#, z#
	Field range#
	Field r%, g%, b%
	
	Field pitch#, yaw#
	Field innerconeangle%, outerconeangle#
End Type 

Function CreateRoomTemplate.RoomTemplates(meshpath$)
	Local rt.RoomTemplates = New RoomTemplates
	
	rt\objPath = meshpath
	
	rt\id = RoomTempID
	RoomTempID=RoomTempID+1
	
	Return rt
End Function

Function LoadRoomTemplates(file$)
	Local TemporaryString$, Temp%, i%, n%
	Local rt.RoomTemplates = Null
	Local StrTemp$ = ""
	
	Local f = OpenFile(file)
	
	While Not Eof(f)
		TemporaryString = Trim(ReadLine(f))
		If Left(TemporaryString,1) = "[" Then
			TemporaryString = Mid(TemporaryString, 2, Len(TemporaryString) - 2)
			StrTemp = GetINIString(file, TemporaryString, "mesh path")
			
			rt = CreateRoomTemplate(StrTemp)
			rt\Name = TemporaryString
			
			StrTemp = GetINIString(file, TemporaryString, "shape")
			Select StrTemp
				Case "room1", "1"
					rt\Shape = ROOM1
				Case "room2", "2"
					rt\Shape = ROOM2
				Case "room2c", "2c", "2C"
					rt\Shape = ROOM2C
				Case "room3", "3"
					rt\Shape = ROOM3
				Case "room4", "4"
					rt\Shape = ROOM4
				Default
					
			End Select
			
			For i = 0 To 4
				rt\zone[i]= GetINIInt(file, TemporaryString, "zone"+(i+1))
				
			Next
			
			rt\Commonness = Max(Min(GetINIInt(file, TemporaryString, "commonness"), 100), 0)
			rt\DisableDecals = GetINIInt(file, TemporaryString, "disabledecals")
			rt\StepSound = GetINIInt(file, TemporaryString, "walksound")
			;rt\ambience = GetINIInt(file, TemporaryString, "ambience")
		EndIf
	Wend
	
	i = 1
	Repeat
		StrTemp = GetINIString(file, "room ambience", "ambience"+i)
		If StrTemp = "" Then Exit
		
		RoomAmbience[i]=LoadSound(StrTemp)
		i=i+1
	Forever
	
	CloseFile f
	
	
	
End Function


Function AddTempLight.LightTemplates(rt.RoomTemplates, x#, y#, z#, ltype%, range#, r%, g%, b%)
	lt.lighttemplates = New LightTemplates
	lt\roomtemplate = rt
	lt\x = x
	lt\y = y
	lt\z = z
	lt\ltype = ltype
	lt\range = range
	lt\r = r
	lt\g = g
	lt\b = b
	
	Return lt
End Function

Function LoadRoomMeshes()
	Local temp% = 0
	For rt.RoomTemplates = Each RoomTemplates
		temp=temp+1
	Next	
	
	Local i = 0
	For rt.RoomTemplates = Each RoomTemplates
		If rt\objpath <> "" Then rt\obj = LoadWorld(rt\objPath, rt) Else rt\obj = CreatePivot()
		If (Not rt\obj) Then RuntimeError "Failed to load map file "+Chr(34)+mapfile+Chr(34)+"."
		
		HideEntity(rt\obj)
		DrawLoading(Int(40 + (20.0 / temp)*i))
		i=i+1
	Next
End Function


;RoomTemplates.Init()
LoadRoomTemplates("Data\rooms.ini")
;RoomTemplates.LoadMeshes()

Global RoomScale# = 8.0 / 2048.0
Const LEVELAMOUNT = 1
Global MapWidth% = GetINIInt("options.ini", "options", "map size"), MapHeight% = GetINIInt("options.ini", "options", "map size")
Dim MapTemp%(LEVELAMOUNT, MapWidth, MapHeight)
Dim MapFound%(LEVELAMOUNT, MapWidth, MapHeight)

Global RoomAmbience%[10]

Global Sky1%, Sky2%

Global HideDistance# = 15.0

Global SecondaryLightOn# = True
Global RemoteDoorOn = True
Global Contained106 = False, Disabled173 = False

Type Rooms
	Field level%
	
	Field found%
	
	Field obj%
	Field x#, y#, z#
	Field angle%
	Field RoomTemplate.RoomTemplates
	
	Field SoundCHN%
	
	Field SoundEmitter%[4]
	Field SoundEmitterObj%[4]
	Field SoundEmitterRange#[4]
	Field SoundEmitterCHN%[4]
	
	Field Lights%[20]
	Field LightIntensity#[20]
	
	Field LightSprites%[20]	
	
	Field Objects%[21]
	Field RoomDoors.Doors[7]
	Field NPC.NPCs[12]
End Type 

Function CreateRoom.Rooms(zone%, roomshape%, x#, y#, z#, name$ = "")
	Local r.Rooms = New Rooms
	Local rt.RoomTemplates
	
	;zone=0
	
	r\level = level
	
	r\x = x : r\y = y : r\z = z
	
	If name <> "" Then
		DebugLog name
		name = Lower(name)
		For rt.RoomTemplates = Each RoomTemplates
			If rt\Name = name Then
				r\RoomTemplate = rt
				;If PlayerLevel = level Then
				r\obj = CopyEntity(rt\obj)	
				ScaleEntity(r\obj, RoomScale, RoomScale, RoomScale)
				EntityType(r\obj, HIT_MAP)
				EntityPickMode(r\obj, 2)
				;Else
				;	r\obj = CreatePivot()
				;EndIf
				
				PositionEntity(r\obj, x, y, z)
				FillRoom(r)
				
				Return r
			EndIf
		Next
	EndIf
	
	Local temp% = 0
	For rt.RoomTemplates = Each RoomTemplates
		
		For i = 0 To 4
			If rt\zone[i]=zone Then 
				If rt\Shape = roomshape Then temp=temp+rt\Commonness : Exit
			EndIf
		Next
		
	Next
	
	DebugLog zone + " - " + roomshape
	
	Local RandomRoom% = Rand(temp)
	temp = 0
	For rt.RoomTemplates = Each RoomTemplates
		For i = 0 To 4
			If rt\zone[i]=zone Then 
				If rt\Shape = roomshape Then
					temp=temp+rt\Commonness
					If RandomRoom > temp - rt\Commonness And RandomRoom <= temp Then
						DebugLog rt\Name+" - "+zone
						DebugLog rt\zone[0]+", "+rt\zone[1]
						r\RoomTemplate = rt
						r\obj = CopyEntity(rt\obj)
						ScaleEntity(r\obj, RoomScale, RoomScale, RoomScale)
						EntityType(r\obj, HIT_MAP)
						EntityPickMode(r\obj, 2)
						
						PositionEntity(r\obj, x, y, z)
						FillRoom(r)
						
						Return r	
					End If
				EndIf
			EndIf
		Next
		
	Next
	
End Function

Function FillRoom(r.Rooms)
	Local d.Doors, d2.Doors, sc.SecurityCams, de.Decals
	Local it.Items, i%
	Local xtemp%, ytemp%, ztemp%
	
	Local t1, t2, bump	
	
	Select r\RoomTemplate\Name
		Case "lockroom"
			d = CreateDoor(r\level, r\x - 736.0 * RoomScale, 0, r\z - 104.0 * RoomScale, 0, r, True)
			d\timer = 70 * 5 : d\AutoClose = False : d\open = False
			
			EntityParent(d\buttons[0], 0)
			PositionEntity(d\buttons[0], r\x - 288.0 * RoomScale, 0.7, r\z - 640.0 * RoomScale)
			EntityParent(d\buttons[0], r\obj)
			
			FreeEntity(d\buttons[1]) : d\buttons[1] = 0
			
			d2 = CreateDoor(r\level, r\x + 104.0 * RoomScale, 0, r\z + 736.0 * RoomScale, 270, r, True)
			d2\timer = 70 * 5 : d2\AutoClose = False: d2\open = False
			EntityParent(d2\buttons[0], 0)
			PositionEntity(d2\buttons[0], r\x + 640.0 * RoomScale, 0.7, r\z + 288.0 * RoomScale)
			RotateEntity (d2\buttons[0], 0, 90, 0)
			EntityParent(d2\buttons[0], r\obj)
			
			FreeEntity(d2\buttons[1]) : d2\buttons[1] = 0
			
			d\LinkedDoor = d2
			d2\LinkedDoor = d
			
			sc.SecurityCams = CreateSecurityCam(r\x - 688.0 * RoomScale, r\y + 384 * RoomScale, r\z + 688.0 * RoomScale, r, True)
			sc\angle = 45 + 180
			sc\turn = 45
			TurnEntity(sc\CameraObj, 40, 0, 0)
			EntityParent(sc\obj, r\obj)
			
			PositionEntity(sc\ScrObj, r\x + 668 * RoomScale, 1.1, r\z - 96.0 * RoomScale)
			TurnEntity(sc\ScrObj, 0, 90, 0)
			EntityParent(sc\ScrObj, r\obj)
			
			sc.SecurityCams = CreateSecurityCam(r\x - 112.0 * RoomScale, r\y + 384 * RoomScale, r\z + 112.0 * RoomScale, r, True)
			sc\angle = 45
			sc\turn = 45
			
			TurnEntity(sc\CameraObj, 40, 0, 0)
			EntityParent(sc\obj, r\obj)				
			
			PositionEntity(sc\ScrObj, r\x + 96.0 * RoomScale, 1.1, r\z - 668.0 * RoomScale)
			EntityParent(sc\ScrObj, r\obj)
			
			Local em.Emitters = CreateEmitter(r\x - 175.0 * RoomScale, 370.0 * RoomScale, r\z + 656.0 * RoomScale, 0)
			TurnEntity(em\Obj, 90, 0, 0, True)
			EntityParent(em\Obj, r\obj)
			em\RandAngle = 15
			em\Speed = 0.025
			em\SizeChange = 0.007
			em\Achange = -0.006
			em\Gravity = -0.24
			
			em.Emitters = CreateEmitter(r\x - 655.0 * RoomScale, 370.0 * RoomScale, r\z + 240.0 * RoomScale, 0)
			TurnEntity(em\Obj, 90, 0, 0, True)
			EntityParent(em\Obj, r\obj)
			em\RandAngle = 15
			em\Speed = 0.025
			em\SizeChange = 0.007
			em\Achange = -0.006
			em\Gravity = -0.24
		Case "lockroom2"
			For i = 0 To 5
				de.Decals = CreateDecal(Rand(2,3), r\x+Rnd(-392,520)*RoomScale, 3.0*RoomScale+Rnd(0,0.001), r\z+Rnd(-392,520)*RoomScale,90,Rnd(360),0)
				de\Size = Rnd(0.3,0.6)
				ScaleSprite(de\obj, de\Size,de\Size)
				CreateDecal(Rand(15,16), r\x+Rnd(-392,520)*RoomScale, 3.0*RoomScale+Rnd(0,0.001), r\z+Rnd(-392,520)*RoomScale,90,Rnd(360),0)
				de\Size = Rnd(0.1,0.6)
				ScaleSprite(de\obj, de\Size,de\Size)
				CreateDecal(Rand(15,16), r\x+Rnd(-0.5,0.5), 3.0*RoomScale+Rnd(0,0.001), r\z+Rnd(-0.5,0.5),90,Rnd(360),0)
				de\Size = Rnd(0.1,0.6)
				ScaleSprite(de\obj, de\Size,de\Size)
			Next
			
			sc.SecurityCams = CreateSecurityCam(r\x + 512.0 * RoomScale, r\y + 384 * RoomScale, r\z + 384.0 * RoomScale, r, True)
			sc\angle = 45 + 90
			sc\turn = 45
			TurnEntity(sc\CameraObj, 40, 0, 0)
			EntityParent(sc\obj, r\obj)
			
			PositionEntity(sc\ScrObj, r\x + 668 * RoomScale, 1.1, r\z - 96.0 * RoomScale)
			TurnEntity(sc\ScrObj, 0, 90, 0)
			EntityParent(sc\ScrObj, r\obj)
			
			sc.SecurityCams = CreateSecurityCam(r\x - 384.0 * RoomScale, r\y + 384 * RoomScale, r\z - 512.0 * RoomScale, r, True)
			sc\angle = 45 + 90 + 180
			sc\turn = 45
			
			TurnEntity(sc\CameraObj, 40, 0, 0)
			EntityParent(sc\obj, r\obj)				
			
			PositionEntity(sc\ScrObj, r\x + 96.0 * RoomScale, 1.1, r\z - 668.0 * RoomScale)
			EntityParent(sc\ScrObj, r\obj)
			
		Case "gatea"
			r\RoomDoors[2] = CreateDoor(r\level, r\x - 4064.0 * RoomScale, (-1280.0+12000.0)*RoomScale, r\z + 3952.0 * RoomScale, 0, r, False)
			r\RoomDoors[2]\AutoClose = False : r\RoomDoors[2]\open = False
			
			d2 = CreateDoor(r\level, r\x, 12000.0*RoomScale, r\z - 1024.0 * RoomScale, 0, r, False)
			d2\AutoClose = False : d2\open = False : d2\locked = True
			
			d2 = CreateDoor(r\level, r\x-1440*RoomScale, (12000.0-480.0)*RoomScale, r\z + 2328.0 * RoomScale, 0, r, False, False, 2)
			d2\AutoClose = False : d2\open = False	
			PositionEntity d2\buttons[0], r\x-1320.0*RoomScale, EntityY(d2\buttons[0],True), r\z + 2288.0*RoomScale, True
			PositionEntity d2\buttons[1], r\x-1584*RoomScale, EntityY(d2\buttons[0],True), r\z + 2488.0*RoomScale, True	
			RotateEntity d2\buttons[1], 0, 90, 0, True
			
			d2 = CreateDoor(r\level, r\x-1440*RoomScale, (12000.0-480.0)*RoomScale, r\z + 4352.0 * RoomScale, 0, r, False, False, 2)
			d2\AutoClose = False : d2\open = False	
			PositionEntity d2\buttons[0], r\x-1320.0*RoomScale, EntityY(d2\buttons[0],True), r\z + 4384.0*RoomScale, True
			RotateEntity d2\buttons[0], 0, 180, 0, True	
			PositionEntity d2\buttons[1], r\x-1584.0*RoomScale, EntityY(d2\buttons[0],True), r\z + 4232.0*RoomScale, True	
			RotateEntity d2\buttons[1], 0, 90, 0, True	
			
			sun = CreateSprite(r\obj)
			;SpriteViewMode(sun,2)
			ScaleSprite(sun, 20, 20)
			PositionEntity (sun, r\x+36.0*RoomScale, 4492.0*RoomScale, r\z+1024.0*RoomScale, True)
			suntex = LoadTexture("GFX\map\sun.jpg")
			EntityTexture sun, suntex
			EntityFX sun, 1+8
			EntityBlend sun, 3	
			
			For r2.rooms = Each Rooms
				If r2\roomtemplate\name = "exit1" Then
					r\Objects[1]=r2\objects[1]
					r\Objects[2]=r2\objects[2]	
				ElseIf r2\roomtemplate\name = "gateaentrance"
					;ylempi hissi
					r\RoomDoors[1] = CreateDoor(0, r\x+1544.0*RoomScale,12000.0*RoomScale, r\z-64.0*RoomScale, 90, r, False)
					r\RoomDoors[1]\AutoClose = False : r\RoomDoors[1]\open = False
					PositionEntity(r\RoomDoors[1]\buttons[0],r\x+1584*RoomScale, EntityY(r\RoomDoors[1]\buttons[0],True), r\z+80*RoomScale, True)
					PositionEntity(r\RoomDoors[1]\buttons[1],r\x+1456*RoomScale, EntityY(r\RoomDoors[1]\buttons[1],True), r\z-208*RoomScale, True)	
					r2\Objects[1] = CreatePivot()
					PositionEntity(r2\Objects[1], r\x+1848.0*RoomScale, 240.0*RoomScale, r\z-64.0*RoomScale, True)
					EntityParent r2\Objects[1], r\obj						
				EndIf
			Next
			
			;106:n spawnpoint
			r\Objects[3]=CreatePivot()
			PositionEntity(r\Objects[3], r\x+1216.0*RoomScale, 0, r\z+2112.0*RoomScale, True)
			EntityParent r\Objects[3], r\obj
			
			;sillan loppupää
			r\Objects[4]=CreatePivot()
			PositionEntity(r\Objects[4], r\x, 96.0*RoomScale, r\z+6400.0*RoomScale, True)
			EntityParent r\Objects[4], r\obj		
			
			;vartiotorni 1
			r\Objects[5]=CreatePivot()
			PositionEntity(r\Objects[5], r\x+1784.0*RoomScale, 2124.0*RoomScale, r\z+4512.0*RoomScale, True)
			EntityParent r\Objects[5], r\obj	
			
			;vartiotorni 2
			r\Objects[6]=CreatePivot()
			PositionEntity(r\Objects[6], r\x-5048.0*RoomScale, 1912.0*RoomScale, r\z+4656.0*RoomScale, True)
			EntityParent r\Objects[6], r\obj	
			
			;sillan takareuna
			r\Objects[7]=CreatePivot()
			PositionEntity(r\Objects[7], r\x+1824.0*RoomScale, 224.0*RoomScale, r\z+7056.0*RoomScale, True)
			EntityParent r\Objects[7], r\obj	
			
			;sillan takareuna2
			r\Objects[8]=CreatePivot()
			PositionEntity(r\Objects[8], r\x-1824.0*RoomScale, 224.0*RoomScale, r\z+7056.0*RoomScale, True)
			EntityParent r\Objects[8], r\obj	
			
			;"valopyssy"
			r\Objects[9]=CreatePivot()
			PositionEntity(r\Objects[9], r\x+2624.0*RoomScale, 992.0*RoomScale, r\z+6157.0*RoomScale, True)
			EntityParent r\Objects[9], r\obj	
			;objects[10] = valopyssyn yläosa
			
			;tunnelin loppu
			r\Objects[11]=CreatePivot()
			PositionEntity(r\Objects[11], r\x-4064.0*RoomScale, -1248.0*RoomScale, r\z-1696.0*RoomScale, True)
			EntityParent r\Objects[11], r\obj
			
			r\Objects[13]=LoadMesh("GFX\map\gateawall1.b3d",r\obj)
			PositionEntity(r\Objects[13], r\x-4308.0*RoomScale, -1045.0*RoomScale, r\z+544.0*RoomScale, True)
			EntityColor r\Objects[13], 25,25,25
			;EntityFX(r\Objects[13],1)
			
			r\Objects[14]=LoadMesh("GFX\map\gateawall2.b3d",r\obj)
			PositionEntity(r\Objects[14], r\x-3820.0*RoomScale, -1045.0*RoomScale, r\z+544.0*RoomScale, True)	
			EntityColor r\Objects[14], 25,25,25
			;EntityFX(r\Objects[14],1)
			
			r\Objects[15]=CreatePivot(r\obj)
			PositionEntity(r\Objects[15], r\x-3568.0*RoomScale, -1089.0*RoomScale, r\z+4944.0*RoomScale, True)	
			
		Case "gateaentrance"
			;alempi hissi
			r\RoomDoors[0] = CreateDoor(0, r\x+744.0*RoomScale, 0, r\z+512.0*RoomScale, 90, r, True)
			r\RoomDoors[0]\AutoClose = False : r\RoomDoors[0]\open = True
			PositionEntity(r\RoomDoors[0]\buttons[1],r\x+688*RoomScale, EntityY(r\RoomDoors[0]\buttons[1],True), r\z+368*RoomScale, True)
			PositionEntity(r\RoomDoors[0]\buttons[0],r\x+784*RoomScale, EntityY(r\RoomDoors[0]\buttons[0],True), r\z+656*RoomScale, True)
			r\Objects[0] = CreatePivot()
			PositionEntity(r\Objects[0], r\x+1048.0*RoomScale, 0, r\z+512.0*RoomScale, True)
			EntityParent r\Objects[0], r\obj
			
		Case "exit1"
			r\Objects[0] = LoadMesh("GFX\map\exit1terrain.b3d", r\obj)
			PositionEntity(r\Objects[0], r\x+4356.0*RoomScale, 9767.0*RoomScale, r\z+2588.0*RoomScale, True)
			ScaleEntity r\Objects[0],RoomScale,RoomScale,RoomScale,True
			
			If BumpEnabled And 0 Then 
				
				Local gravelbump = LoadTexture("GFX\map\gravelbump.jpg")
				TextureBlend gravelbump, FE_BUMP
				
				For i = 1 To CountSurfaces(r\Objects[0])
					sf = GetSurface(r\Objects[0],i)
					b = GetSurfaceBrush( sf )
					t1 = GetBrushTexture(b,0)
					t2 = GetBrushTexture(b,1)
					
					Select StripPath(TextureName(t2))
						Case "gravel.jpg", "grass2.jpg" 
							BrushTexture b, t1, 0, 0
							BrushTexture b, gravelbump, 0, 1
							BrushTexture b, t2, 0, 2	
							
							PaintSurface sf,b
							
							If StripPath(TextureName(t1)) <> "" Then FreeTexture t1
							If StripPath(TextureName(t2)) <> "" Then FreeTexture t2				
							FreeTexture t
							FreeBrush b									
					End Select
					
				Next
			EndIf	
			
			r\RoomDoors[4] = CreateDoor(r\level, r\x, 0, r\z - 320.0 * RoomScale, 0, r, False, True, 3)
			r\RoomDoors[4]\dir = 1 : r\RoomDoors[4]\AutoClose = False : r\RoomDoors[4]\open = False
			PositionEntity(r\RoomDoors[4]\buttons[1], r\x, 8.0, r\z, True)
			;TurnEntity(r\RoomDoors[0]\buttons[0],0,-90,0,True)
			PositionEntity(r\RoomDoors[4]\buttons[0], r\x, 8.0, r\z, True)
			;TurnEntity(r\RoomDoors[0]\buttons[1],0, 90,0,True)
			
			
			;EntityShininess r\Objects[0], 0.3
			
			r\NPC[0] = CreateNPC(NPCtypeApache, r\x, 100.0, r\z)
			r\NPC[0]\State = 1
			
			sun = CreateSprite(r\obj)
			;SpriteViewMode(sun,2)
			ScaleSprite(sun, 20, 20)
			PositionEntity (sun, r\x+11040.0*RoomScale, 15495.0*RoomScale, r\z-6144.0*RoomScale, True)
			suntex = LoadTexture("GFX\map\sun.jpg")
			EntityTexture sun, suntex
			EntityFX sun, 1+8
			EntityBlend sun, 3
			
			r\Objects[1] = LoadTexture("GFX\map\sky.jpg")
			ScaleTexture r\Objects[1], 25.0, 25.0
			
			Sky1 = CreatePlane()
			EntityTexture Sky1, r\Objects[1], 0, 0
			PositionEntity Sky1, 0.0, r\y+14800*RoomScale, 0.0
			TurnEntity Sky1, 180, 0, 0
			EntityFX Sky1, 1
			;EntityParent Sky1, r\obj
			EntityOrder(Sky1,1000)
	;FlipMesh r\Objects[1]
			EntityAlpha Sky1, 0.5
			
			r\Objects[2] = LoadTexture("GFX\map\sky2.jpg")
			ScaleTexture r\Objects[2], 35.0, 35.0
			
			Sky2 = CreatePlane()
			EntityTexture Sky2, r\Objects[2], 0, 0
			PositionEntity Sky2, 0.0, r\y+14900*RoomScale, 0.0
			EntityOrder(Sky2,2000)
			TurnEntity Sky2, 180, 0, 0
			EntityFX Sky2, 1
			;EntityParent Sky2, r\obj
			
			;käytävän takaosa
			r\Objects[3] = CreatePivot()
			PositionEntity(r\Objects[3], r\x-7680.0*RoomScale, 10992.0*RoomScale, r\z-27048.0*RoomScale, True)
			EntityParent r\Objects[3], r\obj
			
			;oikean puolen watchpoint 1
			r\Objects[4] = CreatePivot()
			PositionEntity(r\Objects[4], r\x+1088.0*RoomScale, 12192.0*RoomScale, r\z-4672.0*RoomScale, True)
			EntityParent r\Objects[4], r\obj
			;oikean puolen watchpoint 2
			r\Objects[5] = CreatePivot()
			PositionEntity(r\Objects[5], r\x+3264.0*RoomScale, 12192.0*RoomScale, r\z-4480.0*RoomScale, True)
			EntityParent r\Objects[5], r\obj	
			;vasemman puolen watchpoint 1
			r\Objects[6] = CreatePivot()
			PositionEntity(r\Objects[6], r\x+5192.0*RoomScale, 12192.0*RoomScale, r\z-1760.0*RoomScale, True)
			EntityParent r\Objects[6], r\obj
			;vasemman puolen watchpoint 2
			r\Objects[7] = CreatePivot()
			PositionEntity(r\Objects[7], r\x+5192.0*RoomScale, 12192.0*RoomScale, r\z-4352.0*RoomScale, True)
			EntityParent r\Objects[7], r\obj
			
			;alempi hissi
			r\RoomDoors[0] = CreateDoor(0, r\x+720.0*RoomScale, 0, r\z+1432.0*RoomScale, 0, r, True)
			r\RoomDoors[0]\AutoClose = False : r\RoomDoors[0]\open = True
			MoveEntity r\RoomDoors[0]\buttons[0],0,0,22.0*RoomScale
			MoveEntity r\RoomDoors[0]\buttons[1],0,0,22.0*RoomScale	
			r\Objects[8] = CreatePivot()
			PositionEntity(r\Objects[8], r\x+720.0*RoomScale, 0, r\z+1744.0*RoomScale, True)
			EntityParent r\Objects[8], r\obj
			
			;ylempi hissi
			r\RoomDoors[1] = CreateDoor(0, r\x-5424.0*RoomScale, 10784.0*RoomScale, r\z-1380.0*RoomScale, 0, r, False)
			r\RoomDoors[1]\AutoClose = False : r\RoomDoors[1]\open = False
			MoveEntity r\RoomDoors[1]\buttons[0],0,0,22.0*RoomScale
			MoveEntity r\RoomDoors[1]\buttons[1],0,0,22.0*RoomScale			
			r\Objects[9] = CreatePivot()
			PositionEntity(r\Objects[9], r\x-5424.0*RoomScale, 10784.0*RoomScale, r\z-1068.0*RoomScale, True)
			EntityParent r\Objects[9], r\obj		
			
			r\RoomDoors[2] = CreateDoor(0, r\x+4352.0*RoomScale, 10784.0*RoomScale, r\z-492.0*RoomScale, 0, r, False)
			r\RoomDoors[2]\AutoClose = False : r\RoomDoors[2]\open = False	
			
			r\RoomDoors[3] = CreateDoor(0, r\x+4352.0*RoomScale, 10784.0*RoomScale, r\z+500.0*RoomScale, 0, r, False)
			r\RoomDoors[3]\AutoClose = False : r\RoomDoors[3]\open = False	
			
			;walkway
			r\Objects[10] = CreatePivot()
			PositionEntity(r\Objects[10], r\x+4352.0*RoomScale, 10778.0*RoomScale, r\z+1344.0*RoomScale, True)
			EntityParent r\Objects[10], r\obj	
			
			;"682"
			r\Objects[11] = CreatePivot()
			PositionEntity(r\Objects[11], r\x+2816.0*RoomScale, 11024.0*RoomScale, r\z-2816.0*RoomScale, True)
			EntityParent r\Objects[11], r\obj
			
			;r\Objects[12] = 682:n käsi
			
			;"valvomon" takaovi
			r\RoomDoors[5] = CreateDoor(0, r\x+3248.0*RoomScale, 9856.0*RoomScale, r\z+6400.0*RoomScale, 0, r, False, False, 0, "ABCD")
			r\RoomDoors[5]\AutoClose = False : r\RoomDoors[5]\open = False		
			
			;"valvomon" etuovi
			d.Doors = CreateDoor(0, r\x+3072.0*RoomScale, 9856.0*RoomScale, r\z+5800.0*RoomScale, 90, r, False, False, 3)
			d\AutoClose = False : d\open = False
			
			r\Objects[14] = CreatePivot()
			PositionEntity(r\Objects[14], r\x+3536.0*RoomScale, 10256.0*RoomScale, r\z+5512.0*RoomScale, True)
			EntityParent r\Objects[14], r\obj
			r\Objects[15] = CreatePivot()
			PositionEntity(r\Objects[15], r\x+3536.0*RoomScale, 10256.0*RoomScale, r\z+5824.0*RoomScale, True)
			EntityParent r\Objects[15], r\obj			
			r\Objects[16] = CreatePivot()
			PositionEntity(r\Objects[16], r\x+3856.0*RoomScale, 10256.0*RoomScale, r\z+5512.0*RoomScale, True)
			EntityParent r\Objects[16], r\obj
			r\Objects[17] = CreatePivot()
			PositionEntity(r\Objects[17], r\x+3856.0*RoomScale, 10256.0*RoomScale, r\z+5824.0*RoomScale, True)
			EntityParent r\Objects[17], r\obj
			
			;MTF:n spawnpoint
			r\Objects[18] = CreatePivot()
			PositionEntity(r\Objects[18], r\x+3727.0*RoomScale, 10066.0*RoomScale, r\z+6623.0*RoomScale, True)
			EntityParent r\Objects[18], r\obj
			
			;piste johon helikopterit pakenee nukea
			r\Objects[19] = CreatePivot()
			PositionEntity(r\Objects[19], r\x+3808.0*RoomScale, 12320.0*RoomScale, r\z-13568.0*RoomScale, True)
			EntityParent r\Objects[19], r\obj			
			
		Case "roompj"
			it = CreateItem("Document SCP-372", "paper", r\x + 800.0 * RoomScale, r\y + 256.0 * RoomScale, r\z + 80.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			it = CreateItem("Radio Transceiver", "radio", r\x + 800.0 * RoomScale, r\y + 112.0 * RoomScale, r\z + 944.0 * RoomScale)
			EntityParent(it\obj, r\obj)
		Case "room079"
			d = CreateDoor(r\level, r\x, -448.0*RoomScale, r\z + 1136.0 * RoomScale, 0, r, False,True, 3)
			d\dir = 1 : d\AutoClose = False : d\open = False
			PositionEntity(d\buttons[1], r\x + 224.0 * RoomScale, -250*RoomScale, r\z + 918.0 * RoomScale, True)
			;TurnEntity(d\buttons[0],0,-90,0,True)
			PositionEntity(d\buttons[0], r\x - 240.0 * RoomScale, -250*RoomScale, r\z + 1366.0 * RoomScale, True)
			;TurnEntity(d\buttons[1],0, 90,0,True)	
			
			r\RoomDoors[0] = CreateDoor(r\level, r\x + 1456.0*RoomScale, -448.0*RoomScale, r\z + 976.0 * RoomScale, 0, r, False, True, 3)
			r\RoomDoors[0]\dir = 1 : r\RoomDoors[0]\AutoClose = False : r\RoomDoors[0]\open = False
			PositionEntity(r\RoomDoors[0]\buttons[1], r\x + 1760.0 * RoomScale, -250*RoomScale, r\z + 1236.0 * RoomScale, True)
			TurnEntity(r\RoomDoors[0]\buttons[0],0,-90-90,0,True)
			PositionEntity(r\RoomDoors[0]\buttons[0], r\x + 1760.0 * RoomScale, -240*RoomScale, r\z + 740.0 * RoomScale, True)
			TurnEntity(r\RoomDoors[0]\buttons[1],0, 90-90,0,True)
			
			r\Objects[0] = LoadAnimMesh("GFX\map\079.b3d")
			ScaleEntity(r\Objects[0], 1.3, 1.3, 1.3, True)
			PositionEntity (r\Objects[0], r\x + 1856.0*RoomScale, -560.0*RoomScale, r\z-672.0*RoomScale, True)
			EntityParent(r\Objects[0], r\obj)
			TurnEntity r\Objects[0],0,180,0,True
			
			r\Objects[1] = CreateSprite(r\Objects[0])
			SpriteViewMode(r\Objects[1],2)
			PositionEntity(r\Objects[1], 0.082, 0.119, 0.010)
			ScaleSprite(r\Objects[1],0.18*0.5,0.145*0.5)
			TurnEntity(r\Objects[1],0,13.0,0)
			MoveEntity r\Objects[1], 0,0,-0.022
			EntityTexture (r\Objects[1],OldAiPics(0))
			
			HideEntity r\Objects[1]
			
			r\Objects[2] = CreatePivot(r\obj)
			PositionEntity (r\Objects[2], r\x + 1184.0*RoomScale, -448.0*RoomScale, r\z+1792.0*RoomScale, True)
			
			de.Decals = CreateDecal(3,  r\x + 1184.0*RoomScale, -448.0*RoomScale+0.01, r\z+1792.0*RoomScale,90,Rnd(360),0)
			de\Size = 0.5
			ScaleSprite(de\obj, de\Size,de\Size)
			EntityParent de\obj, r\obj
		Case "room2pit"
			i = 0
			For  xtemp% = -1 To 1 Step 2
				For  ztemp% = -1 To 1
					em.Emitters = CreateEmitter(r\x + 202.0 * RoomScale * xtemp, 8.0 * RoomScale, r\z + 256.0 * RoomScale * ztemp, 0)
					r\Objects[i] = em\Obj
					If i < 3 Then 
						TurnEntity(em\Obj, 0, -90, 0, True) 
					Else 
						TurnEntity(em\Obj, 0, 90, 0, True)
					EndIf
					TurnEntity(em\Obj, -45, 0, 0, True)
					EntityParent(em\Obj, r\obj)
					i=i+1
				Next
			Next
			
			r\Objects[6] = CreatePivot()
			PositionEntity(r\Objects[6], r\x + 640.0 * RoomScale, 8.0 * RoomScale, r\z - 896.0 * RoomScale)
			EntityParent(r\Objects[6], r\obj)
			
			r\Objects[7] = CreatePivot()
			PositionEntity(r\Objects[7], r\x - 864.0 * RoomScale, -400.0 * RoomScale, r\z - 632.0 * RoomScale)
			EntityParent(r\Objects[7],r\obj)
		Case "room2testroom2"
			r\Objects[0] = CreatePivot()
			PositionEntity(r\Objects[0], r\x - 640.0 * RoomScale, 0.5, r\z - 912.0 * RoomScale)
			EntityParent(r\Objects[0], r\obj)
			
			r\Objects[1] = CreatePivot()
			PositionEntity(r\Objects[1], r\x - 632.0 * RoomScale, 0.5, r\z - 16.0 * RoomScale)
			EntityParent(r\Objects[1], r\obj)
			
			Local Glasstex = LoadTexture("GFX\map\glass.png",1+2)
			r\Objects[2] = CreateSprite()
			EntityTexture(r\Objects[2],Glasstex)
			SpriteViewMode(r\Objects[2],2)
			ScaleSprite(r\Objects[2],182.0*RoomScale*0.5, 192.0*RoomScale*0.5)
			PositionEntity(r\Objects[2], r\x - 595.0 * RoomScale, 224.0*RoomScale, r\z - 208.0 * RoomScale)
			TurnEntity(r\Objects[2],0,180,0)			
			EntityParent(r\Objects[2], r\obj)
			
			FreeTexture Glasstex
			
			d = CreateDoor(r\level, r\x - 240.0 * RoomScale, 0.0, r\z + 640.0 * RoomScale, 90, r, False, False)
			d\AutoClose = False : d\open = False		
			
			d = CreateDoor(r\level, r\x - 512.0 * RoomScale, 0.0, r\z + 384.0 * RoomScale, 0, r, False, False)
			d\AutoClose = False : d\open = False					
			
			d = CreateDoor(r\level, r\x - 816.0 * RoomScale, 0.0, r\z - 208.0 * RoomScale, 0, r, False, False)
			d\AutoClose = False : d\open = False
			FreeEntity(d\buttons[0]) : d\buttons[0]=0
			FreeEntity(d\buttons[1]) : d\buttons[1]=0
			
			it = CreateItem("Level 2 Key Card", "key2", r\x - 336.0 * RoomScale, r\y + 144.0 * RoomScale, r\z + 88.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			it = CreateItem("S-NAV 300 Navigator", "nav", r\x - 784.0 * RoomScale, r\y + 160.0 * RoomScale, r\z + 304.0 * RoomScale)
			it\state = 20 : EntityParent(it\obj, r\obj)
		Case "room3tunnel"
			
			r\Objects[0] = CreatePivot(r\obj)
			PositionEntity (r\Objects[0], r\x - 160.0*RoomScale, 4.0*RoomScale, r\z+160.0*RoomScale, True)

		Case "room2toilets"
			r\Objects[0] = CreatePivot()
			PositionEntity(r\Objects[0], r\x + 1040.0 * RoomScale, 192.0 * RoomScale, r\z)
			EntityParent(r\Objects[0], r\obj)
			
			r\Objects[1] = CreatePivot()
			PositionEntity(r\Objects[1], r\x + 1312.0*RoomScale, 0.5, r\z+448.0*RoomScale)
			EntityParent(r\Objects[1], r\obj)			
			
			r\Objects[2] = CreatePivot()
			PositionEntity(r\Objects[2], r\x + 1184.0*RoomScale, 0.01, r\z+448.0*RoomScale)
			EntityParent(r\Objects[2], r\obj)
			
		Case "room2sroom"
			d = CreateDoor(r\level, r\x + 1440.0 * RoomScale, 224.0 * RoomScale, r\z + 32.0 * RoomScale, 90, r, False, False, 4)
			d\AutoClose = False : d\open = False
			
			it = CreateItem("Some SCP-420-J", "420", r\x + 1776.0 * RoomScale, r\y + 400.0 * RoomScale, r\z + 427.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			it = CreateItem("Some SCP-420-J", "420", r\x + 1808.0 * RoomScale, r\y + 400.0 * RoomScale, r\z + 435.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			it = CreateItem("Level 5 Key Card", "key5", r\x + 2232.0 * RoomScale, r\y + 392.0 * RoomScale, r\z + 387.0 * RoomScale)
			RotateEntity it\obj, 0, r\angle, 0, True
			EntityParent(it\obj, r\obj)
			
			it = CreateItem("Nuclear Device Document", "paper", r\x + 2248.0 * RoomScale, r\y + 440.0 * RoomScale, r\z + 372.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			it = CreateItem("Radio Transceiver", "radio", r\x + 2240.0 * RoomScale, r\y + 320.0 * RoomScale, r\z + 128.0 * RoomScale)
			EntityParent(it\obj, r\obj)			
		Case "room2poffices"
			d = CreateDoor(r\level, r\x + 240.0 * RoomScale, 0.0, r\z + 448.0 * RoomScale, 90, r, False, False, 0, Str(AccessCode))
			PositionEntity(d\buttons[0], r\x + 248.0 * RoomScale, EntityY(d\buttons[0],True), EntityZ(d\buttons[0],True),True)
			PositionEntity(d\buttons[1], r\x + 232.0 * RoomScale, EntityY(d\buttons[1],True), EntityZ(d\buttons[1],True),True)			
			d\AutoClose = False : d\open = False
			
			d = CreateDoor(r\level, r\x - 496.0 * RoomScale, 0.0, r\z, 90, r, False, False, 0, "ABCD")
			PositionEntity(d\buttons[0], r\x - 488.0 * RoomScale, EntityY(d\buttons[0],True), EntityZ(d\buttons[0],True),True)
			PositionEntity(d\buttons[1], r\x - 504.0 * RoomScale, EntityY(d\buttons[1],True), EntityZ(d\buttons[1],True),True)				
			d\AutoClose = False : d\open = False : d\locked = True	
			
			d = CreateDoor(r\level, r\x + 240.0 * RoomScale, 0.0, r\z - 576.0 * RoomScale, 90, r, False, False, 0, "7816")
			PositionEntity(d\buttons[0], r\x + 248.0 * RoomScale, EntityY(d\buttons[0],True), EntityZ(d\buttons[0],True),True)
			PositionEntity(d\buttons[1], r\x + 232.0 * RoomScale, EntityY(d\buttons[1],True), EntityZ(d\buttons[1],True),True)		
			d\AutoClose = False : d\open = False	
			
			it = CreateItem("Mysterious Note", "paper", r\x + 736.0 * RoomScale, r\y + 224.0 * RoomScale, r\z + 544.0 * RoomScale)
			EntityParent(it\obj, r\obj)	
			it = CreateItem("Ballistic Vest", "vest", r\x + 608.0 * RoomScale, r\y + 112.0 * RoomScale, r\z + 32.0 * RoomScale)
			EntityParent(it\obj, r\obj) : RotateEntity(it\obj, 0, 90, 0)
			
			it = CreateItem("Incident Report SCP-106-0204", "paper", r\x + 704.0 * RoomScale, r\y + 183.0 * RoomScale, r\z - 576.0 * RoomScale)
			EntityParent(it\obj, r\obj)	
			it = CreateItem("First Aid Kit", "firstaid", r\x + 912.0 * RoomScale, r\y + 112.0 * RoomScale, r\z - 336.0 * RoomScale)
			EntityParent(it\obj, r\obj) : RotateEntity(it\obj, 0, 90, 0)
			
			
		Case "room2nuke"
			;"tuulikaapin" ovi
			d = CreateDoor(r\level, r\x + 576.0 * RoomScale, 0.0, r\z - 152.0 * RoomScale, 90, r, False, False, 5)
			d\AutoClose = False : d\open = False
			PositionEntity(d\buttons[0], r\x + 608.0 * RoomScale, EntityY(d\buttons[0],True), r\z - 284.0 * RoomScale,True)
			PositionEntity(d\buttons[1], r\x + 544.0 * RoomScale, EntityY(d\buttons[1],True), r\z - 284.0 * RoomScale,True)			
			
			d = CreateDoor(r\level, r\x - 544.0 * RoomScale, 1504.0*RoomScale, r\z + 738.0 * RoomScale, 90, r, False, False, 5)
			d\AutoClose = False : d\open = False			
			PositionEntity(d\buttons[0], EntityX(d\buttons[0],True), EntityY(d\buttons[0],True), r\z + 608.0 * RoomScale,True)
			PositionEntity(d\buttons[1], EntityX(d\buttons[1],True), EntityY(d\buttons[1],True), r\z + 608.0 * RoomScale,True)
			
			;yläkerran hissin ovi
			r\RoomDoors[0] = CreateDoor(r\level, r\x + 1192.0 * RoomScale, 0.0, r\z, 90, r, True)
			r\RoomDoors[0]\AutoClose = False : r\RoomDoors[0]\open = True
			;yläkerran hissi
			r\Objects[4] = CreatePivot()
			PositionEntity(r\Objects[4], r\x + 1496.0 * RoomScale, 240.0 * RoomScale, r\z)
			EntityParent(r\Objects[4], r\obj)
			;alakerran hissin ovi
			r\RoomDoors[1] = CreateDoor(r\level, r\x + 680.0 * RoomScale, 1504.0 * RoomScale, r\z, 90, r, False)
			r\RoomDoors[1]\AutoClose = False : r\RoomDoors[1]\open = False
			;alakerran hissi
			r\Objects[5] = CreatePivot()
			PositionEntity(r\Objects[5], r\x + 984.0 * RoomScale, 1744.0 * RoomScale, r\z)
			EntityParent(r\Objects[5], r\obj)
			
			For n% = 0 To 1
				r\Objects[n * 2] = CopyEntity(LeverBaseOBJ)
				r\Objects[n * 2 + 1] = CopyEntity(LeverOBJ)
				
				For i% = 0 To 1
					ScaleEntity(r\Objects[n * 2 + i], 0.04, 0.04, 0.04)
					PositionEntity (r\Objects[n * 2 + i], r\x - 975.0 * RoomScale, r\y + 1712.0 * RoomScale, r\z - (502.0-132.0*n) * RoomScale, True)
					
					EntityParent(r\Objects[n * 2 + i], r\obj)
				Next
				RotateEntity(r\Objects[n * 2], 0, -90-180, 0)
				RotateEntity(r\Objects[n * 2 + 1], 10, -90 - 180-180, 0)
				
				;EntityPickMode(r\Objects[n * 2 + 1], 2)
				EntityPickMode r\Objects[n * 2 + 1], 1, False
				EntityRadius r\Objects[n * 2 + 1], 0.1
				;makecollbox(r\Objects[n * 2 + 1])
			Next
			
			it = CreateItem("Nuclear Device Document", "paper", r\x - 768.0 * RoomScale, r\y + 1684.0 * RoomScale, r\z - 768.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			it = CreateItem("Ballistic Vest", "vest", r\x - 944.0 * RoomScale, r\y + 1652.0 * RoomScale, r\z - 656.0 * RoomScale)
			EntityParent(it\obj, r\obj) : RotateEntity(it\obj, 0, -90, 0)
			
		Case "room2tunnel"
			r\Objects[0] = CreatePivot()
			PositionEntity(r\Objects[0], r\x + 2640.0 * RoomScale, -2496.0 * RoomScale, r\z + 400.0 * RoomScale)
			EntityParent(r\Objects[0], r\obj)
			
			r\Objects[1] = CreatePivot()
			PositionEntity(r\Objects[1], r\x - 4336.0 * RoomScale, -2496.0 * RoomScale, r\z - 2512.0 * RoomScale)
			EntityParent(r\Objects[1], r\obj)
			
			r\Objects[2] = CreatePivot()
			PositionEntity(r\Objects[2], r\x + 552.0 * RoomScale, 240.0 * RoomScale, r\z + 656.0 * RoomScale)
			EntityParent(r\Objects[2], r\obj)
			
			r\Objects[3] = CreatePivot()
			PositionEntity(r\Objects[3], r\x - 2040.0 * RoomScale, -2256.0 * RoomScale, r\z - 656.0 * RoomScale)
			EntityParent(r\Objects[3], r\obj)			
			
			r\Objects[4] = CreatePivot()
			PositionEntity(r\Objects[4], r\x - 552.0 * RoomScale, 240.0 * RoomScale, r\z - 656.0 * RoomScale)
			EntityParent(r\Objects[4], r\obj)
			
			r\Objects[5] = CreatePivot()
			PositionEntity(r\Objects[5], r\x + 2072.0 * RoomScale, -2256.0 * RoomScale, r\z + 656.0 * RoomScale)
			EntityParent(r\Objects[5], r\obj)			
			
			r\RoomDoors[0] = CreateDoor(r\level, r\x + 264.0 * RoomScale, 0.0, r\z + 656.0 * RoomScale, 90, r, True)
			r\RoomDoors[0]\AutoClose = False : r\RoomDoors[0]\open = True
			PositionEntity(r\RoomDoors[0]\buttons[1], r\x + 224.0 * RoomScale, 0.7, r\z + 480.0 * RoomScale, True)
			PositionEntity(r\RoomDoors[0]\buttons[0], r\x + 304.0 * RoomScale, 0.7, r\z + 832.0 * RoomScale, True)			
			
			r\RoomDoors[1] = CreateDoor(r\level, r\x - 2328.0 * RoomScale, -2496.0 * RoomScale, r\z - 656.0 * RoomScale, 90, r, False)
			r\RoomDoors[1]\AutoClose = False : r\RoomDoors[1]\open = False	
			PositionEntity(r\RoomDoors[1]\buttons[0], r\x - 2288.0 * RoomScale, EntityY(r\RoomDoors[1]\buttons[0],True), r\z - 480.0 * RoomScale, True)
			PositionEntity(r\RoomDoors[1]\buttons[1], r\x - 2368.0 * RoomScale, EntityY(r\RoomDoors[1]\buttons[1],True), r\z - 800.0 * RoomScale, True)				
			
			r\RoomDoors[2] = CreateDoor(r\level, r\x - 264.0 * RoomScale, 0.0, r\z - 656.0 * RoomScale, 90, r, True)
			r\RoomDoors[2]\AutoClose = False : r\RoomDoors[2]\open = True
			PositionEntity(r\RoomDoors[2]\buttons[0], r\x - 224.0 * RoomScale, 0.7, r\z - 480.0 * RoomScale, True)
			PositionEntity(r\RoomDoors[2]\buttons[1], r\x - 304.0 * RoomScale, 0.7, r\z - 832.0 * RoomScale, True)				
			
			r\RoomDoors[3] = CreateDoor(r\level, r\x + 2360.0 * RoomScale, -2496.0 * RoomScale, r\z + 656.0 * RoomScale, 90, r, False)
			r\RoomDoors[3]\AutoClose = False : r\RoomDoors[3]\open = False		
			PositionEntity(r\RoomDoors[3]\buttons[1], r\x + 2320.0 * RoomScale, EntityY(r\RoomDoors[3]\buttons[0],True), r\z + 480.0 * RoomScale, True)
			PositionEntity(r\RoomDoors[3]\buttons[0], r\x + 2432.0 * RoomScale, EntityY(r\RoomDoors[3]\buttons[1],True), r\z + 816.0 * RoomScale, True)				
			
			d.Doors = CreateDoor(0, r\x,r\y,r\z,0, r, False, True, False, "ABCD")
			PositionEntity(d\buttons[0], r\x + 224.0 * RoomScale, r\y + 0.7, r\z - 384.0 * RoomScale, True)
			RotateEntity (d\buttons[0], 0,-90,0,True)
			PositionEntity(d\buttons[1], r\x - 224.0 * RoomScale, r\y + 0.7, r\z + 384.0 * RoomScale, True)		
			RotateEntity (d\buttons[1], 0,90,0,True)
			
			it = CreateItem("SCP-500-01", "scp500", r\x + 2848.0 * RoomScale, r\y -2368.0 * RoomScale, r\z - 4568.0 * RoomScale)
			EntityParent(it\obj, r\obj) : RotateEntity(it\obj, 0, 180, 0)
			
			it = CreateItem("First Aid Kit", "firstaid", r\x + 2648.0 * RoomScale, r\y -2158.0 * RoomScale, r\z - 4568.0 * RoomScale)
			EntityParent(it\obj, r\obj)
		Case "008"
			
			r\Objects[0] = CreatePivot(r\obj)
			PositionEntity(r\Objects[0], r\x + 292.0 * RoomScale, 120.0*RoomScale, r\z + 708.0 * RoomScale, True)
			
			r\Objects[1] = LoadMesh("GFX\map\008_2.b3d")
			ScaleEntity r\Objects[1], RoomScale, RoomScale, RoomScale
			PositionEntity(r\Objects[1], r\x + 292 * RoomScale, 120 * RoomScale, r\z + 768.0 * RoomScale, 0)
			EntityParent(r\Objects[1], r\obj)
			
			;r\Objects[1] = CreatePivot()
			;PositionEntity(r\Objects[1], r\x + 396.0 * RoomScale, 0.5, r\z + 1038.0 * RoomScale)
			;EntityParent(r\Objects[1], r\obj)
			
			d = CreateDoor(r\level, r\x + 296.0 * RoomScale, 0, r\z - 672.0 * RoomScale, 180, r, False, 0, 4)
			d\AutoClose = False ;: d\buttons[0] = False
			PositionEntity (d\buttons[1], r\x + 164.0 * RoomScale, EntityY(d\buttons[1],True), r\z - 672.0 * RoomScale, True)
			FreeEntity d\buttons[0] : d\buttons[0]=0
			FreeEntity d\obj2 : d\obj2=0
			
			d2 = CreateDoor(r\level, r\x + 296.0 * RoomScale, 0, r\z - 144.0 * RoomScale, 0, r, True)
			d2\AutoClose = False ;: d\buttons[0] = False
			PositionEntity (d2\buttons[0], r\x + 432.0 * RoomScale, EntityY(d2\buttons[0],True), r\z - 480.0 * RoomScale, True)
			RotateEntity(d2\buttons[0], 0, -90, 0, True)			
			PositionEntity (d2\buttons[1], r\x + 164.0 * RoomScale, EntityY(d2\buttons[0],True), r\z - 128.0 * RoomScale, True)
			FreeEntity d2\obj2 : d2\obj2=0
			
			d\LinkedDoor = d2
			d2\LinkedDoor = d
			
			d = CreateDoor(r\level, r\x - 384.0 * RoomScale, 0, r\z - 672.0 * RoomScale, 0, r, False, 0, 4)
			d\AutoClose = False
			
			it = CreateItem("Hazmat Suit", "hazmatsuit", r\x - 76.0 * RoomScale, 0.5, r\z - 396.0 * RoomScale)
			EntityParent(it\obj, r\obj) : RotateEntity(it\obj, 0, 90, 0)
			
			it = CreateItem("Document SCP-008", "paper", r\x - 245.0 * RoomScale, r\y + 192.0 * RoomScale, r\z + 368.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			;spawnpoint for the scientist used in the "008 zombie scene"
			r\Objects[2] = CreatePivot(r\obj)
			PositionEntity(r\Objects[2], r\x + 160 * RoomScale, 672 * RoomScale, r\z - 384.0 * RoomScale, True)
			;spawnpoint for the player
			r\Objects[3] = CreatePivot(r\obj)
			PositionEntity(r\Objects[3], r\x, 672 * RoomScale, r\z + 352.0 * RoomScale, True)
			
		Case "room049"
			
			r\Objects[0] = CreatePivot(r\obj)
			PositionEntity(r\Objects[0], r\x + 640.0 * RoomScale, 240.0 * RoomScale, r\z + 656.0 * RoomScale, True)
			
			r\Objects[1] = CreatePivot(r\obj)
			PositionEntity(r\Objects[1], r\x - 2032.0 * RoomScale, -3280.0 * RoomScale, r\z - 656.0 * RoomScale, True)
			
			r\Objects[2] = CreatePivot(r\obj)
			PositionEntity(r\Objects[2], r\x - 640.0 * RoomScale, 240.0 * RoomScale, r\z - 656.0 * RoomScale, True)
			
			r\Objects[3] = CreatePivot(r\obj)
			PositionEntity(r\Objects[3], r\x + 2040.0 * RoomScale, -3280.0 * RoomScale, r\z + 656.0 * RoomScale, True)
			
			;storage room (the spawn point of scp-049)
			r\Objects[5] = CreatePivot(r\obj)
			PositionEntity(r\Objects[5], r\x + 584.0 * RoomScale, -3440.0 * RoomScale, r\z + 104.0 * RoomScale, True)
			
			;zombie 1
			r\Objects[6] = CreatePivot(r\obj)
			PositionEntity(r\Objects[6], r\x - 96.0 * RoomScale, -3440.0 * RoomScale, r\z + 1032.0 * RoomScale, True)
			;zombie 2
			r\Objects[7] = CreatePivot(r\obj)
			PositionEntity(r\Objects[7], r\x  + 64.0 * RoomScale, -3440.0 * RoomScale, r\z - 752.0 * RoomScale, True)
			
			;049's chamber, the trigger for spawning 049
			r\Objects[8] = CreatePivot(r\obj)
			PositionEntity(r\Objects[8], r\x - 1408.0 * RoomScale, -3440.0 * RoomScale, r\z + 16.0 * RoomScale, True)
			
			
			r\RoomDoors[0] = CreateDoor(r\level, r\x + 328.0 * RoomScale, 0.0, r\z + 656.0 * RoomScale, 90, r, True)
			r\RoomDoors[0]\AutoClose = False : r\RoomDoors[0]\open = True
			PositionEntity(r\RoomDoors[0]\buttons[1], r\x + 288.0 * RoomScale, 0.7, r\z + 512.0 * RoomScale, True)
			PositionEntity(r\RoomDoors[0]\buttons[0], r\x + 368.0 * RoomScale, 0.7, r\z + 840.0 * RoomScale, True)			
			
			r\RoomDoors[1] = CreateDoor(r\level, r\x - 2328.0 * RoomScale, -3520.0 * RoomScale, r\z - 656.0 * RoomScale, 90, r, False)
			r\RoomDoors[1]\AutoClose = False : r\RoomDoors[1]\open = False	
			PositionEntity(r\RoomDoors[1]\buttons[1], r\x - 2432.0 * RoomScale, EntityY(r\RoomDoors[1]\buttons[1],True), r\z - 816.0 * RoomScale, True)
			PositionEntity(r\RoomDoors[1]\buttons[0], r\x - 2304.0 * RoomScale, EntityY(r\RoomDoors[1]\buttons[0],True), r\z - 472.0 * RoomScale, True)				
			
			r\RoomDoors[2] = CreateDoor(r\level, r\x - 328.0 * RoomScale, 0.0, r\z - 656.0 * RoomScale, 90, r, True)
			r\RoomDoors[2]\AutoClose = False : r\RoomDoors[2]\open = True
			PositionEntity(r\RoomDoors[2]\buttons[0], r\x - 288.0 * RoomScale, 0.7, r\z - 512.0 * RoomScale, True)
			PositionEntity(r\RoomDoors[2]\buttons[1], r\x - 368.0 * RoomScale, 0.7, r\z - 840.0 * RoomScale, True)				
			
			r\RoomDoors[3] = CreateDoor(r\level, r\x + 2360.0 * RoomScale, -3520.0 * RoomScale, r\z + 656.0 * RoomScale, 90, r, False)
			r\RoomDoors[3]\AutoClose = False : r\RoomDoors[3]\open = False		
			PositionEntity(r\RoomDoors[3]\buttons[0], r\x + 2432.0 * RoomScale, EntityY(r\RoomDoors[3]\buttons[0],True), r\z + 816.0 * RoomScale, True)
			PositionEntity(r\RoomDoors[3]\buttons[1], r\x + 2312.0 * RoomScale, EntityY(r\RoomDoors[3]\buttons[1],True), r\z + 472.0 * RoomScale, True)				
			
			;storage room door
			r\RoomDoors[4] = CreateDoor(r\level, r\x + 272.0 * RoomScale, -3552.0 * RoomScale, r\z + 104.0 * RoomScale, 90, r, False)
			r\RoomDoors[4]\AutoClose = False : r\RoomDoors[4]\open = False : r\RoomDoors[4]\locked = True
			
			d.Doors = CreateDoor(0, r\x,0,r\z, 0, r, False, 2, False, "ABCD")
			
			it = CreateItem("Document SCP-049", "paper", r\x - 608.0 * RoomScale, r\y - 3332.0 * RoomScale, r\z + 876.0 * RoomScale)
			EntityParent(it\obj, r\obj)
		Case "room012"
			d.Doors = CreateDoor(r\level, r\x + 264.0 * RoomScale, 0.0, r\z + 672.0 * RoomScale, 270, r, False, False, 3)
			PositionEntity(d\buttons[0], r\x + 224.0 * RoomScale, EntityY(d\buttons[0],True), r\z + 880.0 * RoomScale, True)
			PositionEntity(d\buttons[1], r\x + 304.0 * RoomScale, EntityY(d\buttons[1],True), r\z + 840.0 * RoomScale, True)	
			TurnEntity d\buttons[1],0,0,0,True
			
			r\RoomDoors[0] = CreateDoor(r\level, r\x -512.0 * RoomScale, -768.0*RoomScale, r\z -336.0 * RoomScale, 0, r, False, False)
			r\RoomDoors[0]\AutoClose = False : r\RoomDoors[0]\open = False : r\RoomDoors[0]\locked = True
			PositionEntity(r\RoomDoors[0]\buttons[0], r\x + 176.0 * RoomScale, -512.0*RoomScale, r\z - 364.0 * RoomScale, True)
			FreeEntity r\RoomDoors[0]\buttons[1] : r\RoomDoors[0]\buttons[1]=0
			
			r\Objects[0] = CopyEntity(LeverBaseOBJ)
			r\Objects[1] = CopyEntity(LeverOBJ)
			
			For i% = 0 To 1
				ScaleEntity(r\Objects[i], 0.04, 0.04, 0.04)
				PositionEntity (r\Objects[i], r\x + 240.0 * RoomScale, r\y - 512.0 * RoomScale, r\z - 364 * RoomScale, True)
				
				EntityParent(r\Objects[i], r\obj)
			Next
			;RotateEntity(r\Objects[0], 0, 0, 0)
			RotateEntity(r\Objects[1], 10, -180, 0)
			
			EntityPickMode r\Objects[1], 1, False
			EntityRadius r\Objects[1], 0.1
			
			r\Objects[2] = LoadMesh("GFX\map\room012_2.b3d")
			ScaleEntity r\Objects[2], RoomScale, RoomScale, RoomScale
			PositionEntity(r\Objects[2], r\x - 360 * RoomScale, - 130 * RoomScale, r\z + 456.0 * RoomScale, 0)
			EntityParent(r\Objects[2], r\obj)
			
			r\Objects[3] = CreateSprite()
			PositionEntity(r\Objects[3], r\x - 43.5 * RoomScale, - 574 * RoomScale, r\z - 362.0 * RoomScale)
			ScaleSprite(r\Objects[3], 0.015, 0.015)
			EntityTexture(r\Objects[3], LightSpriteTex(1))
			EntityBlend (r\Objects[3], 3)
			EntityParent(r\Objects[3], r\obj)
			HideEntity r\Objects[3]
			
			r\Objects[4] = LoadMesh("GFX\map\room012_3.b3d")
			tex=LoadTexture("GFX\map\scp-012_0.jpg")
			EntityTexture r\Objects[4],tex, 0,1
			ScaleEntity r\Objects[4], RoomScale, RoomScale, RoomScale
			PositionEntity(r\Objects[4], r\x - 360 * RoomScale, - 130 * RoomScale, r\z + 456.0 * RoomScale, 0)
			EntityParent(r\Objects[4], r\Objects[2])
			
			it = CreateItem("Document SCP-012", "paper", r\x - 56.0 * RoomScale, r\y - 576.0 * RoomScale, r\z - 408.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
		Case "endroom"
			sc.SecurityCams = CreateSecurityCam(r\x, r\y + 706*RoomScale, r\z + 64*RoomScale, r)
			sc\FollowPlayer = True
			
		Case "tunnel2"
			r\Objects[0] = CreatePivot(r\obj)
			PositionEntity(r\Objects[0], r\x, 544.0 * RoomScale, r\z + 512.0 * RoomScale, True)
			EntityParent(r\Objects[0], r\obj)			
			
			r\Objects[1] = CreatePivot(r\obj)
			PositionEntity(r\Objects[1], r\x, 544.0 * RoomScale, r\z - 512.0 * RoomScale, True)
			
		Case "room2pipes"
			r\Objects[0]= CreatePivot(r\obj)
			PositionEntity(r\Objects[0], r\x + 368.0 * RoomScale, 0.0, r\z, True)
			
			r\Objects[1] = CreatePivot(r\obj)
			PositionEntity(r\Objects[1], r\x - 368.0 * RoomScale, 0.0, r\z, True)
			
			r\Objects[2] = CreatePivot(r\obj)
			PositionEntity(r\Objects[2], r\x + 224.0 * RoomScale - 0.005, 192.0 * RoomScale, r\z, True)
			
			r\Objects[3] = CreatePivot(r\obj)
			PositionEntity(r\Objects[3], r\x - 224.0 * RoomScale + 0.005, 192.0 * RoomScale, r\z, True)
		Case "room3pit"
			em.Emitters = CreateEmitter(r\x + 512.0 * RoomScale, -76 * RoomScale, r\z - 688 * RoomScale, 0)
			TurnEntity(em\Obj, -90, 0, 0)
			EntityParent(em\Obj, r\obj)
			em\RandAngle = 35
			
			em.Emitters = CreateEmitter(r\x - 512.0 * RoomScale, -76 * RoomScale, r\z - 688 * RoomScale, 0)
			TurnEntity(em\Obj, -90, 0, 0)
			EntityParent(em\Obj, r\obj)
			em\RandAngle = 35
			
			r\Objects[0]= CreatePivot(r\obj)
			PositionEntity(r\Objects[0], r\x + 704.0 * RoomScale, 112.0*RoomScale, r\z-416.0*RoomScale, True)
		Case "room2servers"
			d.Doors = CreateDoor(0, r\x,0,r\z, 0, r, False, 2, False)
			d\locked = True
			
			r\RoomDoors[0] = CreateDoor(r\level, r\x - 208.0 * RoomScale, 0.0, r\z - 736.0 * RoomScale, 90, r, True, False)
			r\RoomDoors[0]\AutoClose=False
			r\RoomDoors[1] = CreateDoor(r\level, r\x - 208.0 * RoomScale, 0.0, r\z + 736.0 * RoomScale, 90, r, True, False)
			r\RoomDoors[1]\AutoClose=False
			
			For n% = 0 To 2
				r\Objects[n * 2] = CopyEntity(LeverBaseOBJ)
				r\Objects[n * 2 + 1] = CopyEntity(LeverOBJ)
				
				For i% = 0 To 1
					ScaleEntity(r\Objects[n * 2 + i], 0.03, 0.03, 0.03)
					
					Select n
						Case 0 ;power switch
							PositionEntity (r\Objects[n * 2 + i], r\x - 1260.0 * RoomScale, r\y + 234.0 * RoomScale, r\z + 750 * RoomScale, True)	
						Case 1 ;generator fuel pump
							PositionEntity (r\Objects[n * 2 + i], r\x - 920.0 * RoomScale, r\y + 164.0 * RoomScale, r\z + 898 * RoomScale, True)
						Case 2 ;generator on/off
							PositionEntity (r\Objects[n * 2 + i], r\x - 837.0 * RoomScale, r\y + 152.0 * RoomScale, r\z + 886 * RoomScale, True)
					End Select
					
					EntityParent(r\Objects[n * 2 + i], r\obj)
				Next
				;RotateEntity(r\Objects[n * 2], 0, -90, 0)
				RotateEntity(r\Objects[n*2+1], 81, -180, 0)
				
				;EntityPickMode(r\Objects[n * 2 + 1], 2)
				EntityPickMode r\Objects[n * 2 + 1], 1, False
				EntityRadius r\Objects[n * 2 + 1], 0.1
				;makecollbox(r\Objects[n * 2 + 1])
			Next
			
			RotateEntity(r\Objects[2+1], -81, -180, 0)
			RotateEntity(r\Objects[4+1], -81, -180, 0)
			
			;096 spawnpoint
			r\Objects[6]=CreatePivot(r\obj)
			PositionEntity(r\Objects[6], r\x - 512.0 * RoomScale, 0.5, r\z, True)
			;guard spawnpoint
			r\Objects[7]=CreatePivot(r\obj)
			PositionEntity(r\Objects[7], r\x - 1328.0 * RoomScale, 0.5, r\z + 528*RoomScale, True)
			;the point where the guard walks to
			r\Objects[8]=CreatePivot(r\obj)
			PositionEntity(r\Objects[8], r\x - 1376.0 * RoomScale, 0.5, r\z + 32*RoomScale, True)
			
			
		Case "room3servers"
			it = CreateItem("9V Battery", "bat", r\x - 132.0 * RoomScale, r\y - 368.0 * RoomScale, r\z - 648.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			If Rand(2) = 1 Then
				it = CreateItem("9V Battery", "bat", r\x - 76.0 * RoomScale, r\y - 368.0 * RoomScale, r\z - 648.0 * RoomScale)
				EntityParent(it\obj, r\obj)
			EndIf
			If Rand(2) = 1 Then
				it = CreateItem("9V Battery", "bat", r\x - 196.0 * RoomScale, r\y - 368.0 * RoomScale, r\z - 648.0 * RoomScale)
				EntityParent(it\obj, r\obj)
			EndIf
			
			it = CreateItem("S-NAV 300 Navigator", "nav", r\x + 124.0 * RoomScale, r\y - 368.0 * RoomScale, r\z - 648.0 * RoomScale)
			it\state = 20 : EntityParent(it\obj, r\obj)
			
			r\Objects[0] = CreatePivot(r\obj)
			PositionEntity(r\Objects[0], r\x + 736.0 * RoomScale, -512.0 * RoomScale, r\z - 400.0 * RoomScale, True)
			r\Objects[1] = CreatePivot(r\obj)
			PositionEntity(r\Objects[1], r\x - 552.0 * RoomScale, -512.0 * RoomScale, r\z - 528.0 * RoomScale, True)			
			r\Objects[2] = CreatePivot(r\obj)
			PositionEntity(r\Objects[2], r\x + 736.0 * RoomScale, -512.0 * RoomScale, r\z + 272.0 * RoomScale, True)
			
			r\Objects[3] = LoadMesh("GFX\npcs\duck_low_res.b3d")
			ScaleEntity(r\Objects[3], 0.07, 0.07, 0.07)
			tex = LoadTexture("GFX\npcs\duck2.png")
			EntityTexture r\Objects[3], tex
			PositionEntity (r\Objects[3], r\x + 928.0 * RoomScale, -640*RoomScale, r\z + 704.0 * RoomScale)
			
			EntityParent r\Objects[3], r\obj
			
		Case "room3servers2"
			r\Objects[0] = CreatePivot(r\obj)
			PositionEntity(r\Objects[0], r\x - 504.0 * RoomScale, -512.0 * RoomScale, r\z + 271.0 * RoomScale, True)
			r\Objects[1] = CreatePivot(r\obj)
			PositionEntity(r\Objects[1], r\x + 628.0 * RoomScale, -512.0 * RoomScale, r\z + 271.0 * RoomScale, True)			
			r\Objects[2] = CreatePivot(r\obj)
			PositionEntity(r\Objects[2], r\x - 532.0 * RoomScale, -512.0 * RoomScale, r\z - 877.0 * RoomScale, True)	
			
		Case "testroom"
			For xtemp = 0 To 1
				For ztemp = -1 To 1
					r\Objects[xtemp * 3 + (ztemp + 1)] = CreatePivot()
					PositionEntity(r\Objects[xtemp * 3 + (ztemp + 1)], r\x + (-236.0 + 280.0 * xtemp) * RoomScale, -700.0 * RoomScale, r\z + 384.0 * ztemp * RoomScale)
					EntityParent(r\Objects[xtemp * 3 + (ztemp + 1)], r\obj)
				Next
			Next
			
			r\Objects[6] = CreatePivot()
			PositionEntity(r\Objects[6], r\x + 754.0 * RoomScale, r\y - 1248.0 * RoomScale, r\z)
			EntityParent(r\Objects[6], r\obj)
			
			sc.SecurityCams = CreateSecurityCam(r\x + 744.0 * RoomScale, r\y - 856.0 * RoomScale, r\z + 236.0 * RoomScale, r)
			sc\FollowPlayer = True
			
			d = CreateDoor(r\level, r\x - 624.0 * RoomScale, -1280.0 * RoomScale, r\z, 90, r, True)			
			
			it = CreateItem("Document SCP-682", "paper", r\x + 656.0 * RoomScale, r\y - 1200.0 * RoomScale, r\z - 16.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
		Case "room2closets"
			it = CreateItem("Document SCP-173", "paper", r\x - 172.0 * RoomScale, r\y - 112.0 * RoomScale, r\z + 403.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			it = CreateItem("Gas Mask", "gasmask", r\x + 184.0 * RoomScale, r\y - 272.0 * RoomScale, r\z + 480.0 * RoomScale)
			ScaleEntity(it\obj, 0.02, 0.02, 0.02) : EntityParent(it\obj, r\obj)
			
			it = CreateItem("9V Battery", "bat", r\x + 184.0 * RoomScale, r\y - 224.0 * RoomScale, r\z - 496.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			If Rand(2) = 1 Then
				it = CreateItem("9V Battery", "bat", r\x + 184.0 * RoomScale, r\y - 224.0 * RoomScale, r\z - 528.0 * RoomScale)
				EntityParent(it\obj, r\obj)
			EndIf
			If Rand(2) = 1 Then
				it = CreateItem("9V Battery", "bat", r\x + 184.0 * RoomScale, r\y - 224.0 * RoomScale, r\z - 592.0 * RoomScale)
				EntityParent(it\obj, r\obj)
			EndIf
			
			r\Objects[0]=CreatePivot(r\obj)
			PositionEntity r\Objects[0], r\x-1120*RoomScale, -256*RoomScale, r\z+896*RoomScale, True
			r\Objects[1]=CreatePivot(r\obj)
			PositionEntity r\Objects[1], r\x-1232*RoomScale, -256*RoomScale, r\z-160*RoomScale, True
			
			d.Doors = CreateDoor(0, r\x - 240.0 * RoomScale, 0.0, r\z, 90, r, False)
			d\open = False : d\AutoClose = False 
			
			sc.SecurityCams = CreateSecurityCam(r\x, r\y + 704*RoomScale, r\z + 863*RoomScale, r)
			sc\FollowPlayer = True
			
		Case "room2offices"
			it = CreateItem("Document SCP-106", "paper", r\x + 404.0 * RoomScale, r\y + 145.0 * RoomScale, r\z + 559.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			it = CreateItem("Level 2 Key Card", "key2", r\x - 156.0 * RoomScale, r\y + 151.0 * RoomScale, r\z + 72.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			it = CreateItem("S-NAV 300 Navigator", "nav", r\x + 372.0 * RoomScale, r\y + 153.0 * RoomScale, r\z + 944.0 * RoomScale)
			it\state = 20 : EntityParent(it\obj, r\obj)
			
			it = CreateItem("Notification", "paper", r\x -137.0 * RoomScale, r\y + 153.0 * RoomScale, r\z + 464.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			w.waypoints = CreateWaypoint(r\x - 32.0 * RoomScale, r\y + 66.0 * RoomScale, r\z + 288.0 * RoomScale, Null, r)
			w2.waypoints = CreateWaypoint(r\x, r\y + 66.0 * RoomScale, r\z - 448.0 * RoomScale, Null, r)
			w\connected[0] = w2 : w\dist[0] = EntityDistance(w\obj, w2\obj)
			w2\connected[0] = w : w2\dist[0] = w\dist[0]
			
		Case "room2offices2"
			it = CreateItem("Level 1 Key Card", "key1", r\x - 368.0 * RoomScale, r\y - 48.0 * RoomScale, r\z + 80.0 * RoomScale)
			Local texture% = LoadTexture("GFX\items\keycard1.jpg")
			EntityTexture(it\obj, texture)
			EntityParent(it\obj, r\obj)
			
			it = CreateItem("Document SCP-895", "paper", r\x - 800.0 * RoomScale, r\y - 48.0 * RoomScale, r\z + 368.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			If Rand(2) = 1 Then
				it = CreateItem("Document SCP-860", "paper", r\x - 800.0 * RoomScale, r\y - 48.0 * RoomScale, r\z - 464.0 * RoomScale)
			Else
				it = CreateItem("SCP-093 Recovered Materials", "paper", r\x - 800.0 * RoomScale, r\y - 48.0 * RoomScale, r\z - 464.0 * RoomScale)
			EndIf
			EntityParent(it\obj, r\obj)
			
			it = CreateItem("S-NAV 300 Navigator", "nav", r\x - 336.0 * RoomScale, r\y - 48.0 * RoomScale, r\z - 480.0 * RoomScale)
			it\state = 28 : EntityParent(it\obj, r\obj)		
			
			r\Objects[0] = LoadMesh("GFX\npcs\duck_low_res.b3d")
			ScaleEntity(r\Objects[0], 0.07, 0.07, 0.07)
			
			EntityParent(r\Objects[0], r\obj)
			
			r\Objects[1] = CreatePivot(r\obj)
			PositionEntity(r\Objects[1], r\x-808.0 * RoomScale, -72.0 * RoomScale, r\z - 40.0 * RoomScale, True)
			r\Objects[2] = CreatePivot(r\obj)
			PositionEntity(r\Objects[2], r\x-488.0 * RoomScale, 160.0 * RoomScale, r\z + 700.0 * RoomScale, True)
			r\Objects[3] = CreatePivot(r\obj)
			PositionEntity(r\Objects[3], r\x-488.0 * RoomScale, 160.0 * RoomScale, r\z - 668.0 * RoomScale, True)
			r\Objects[4] = CreatePivot(r\obj)
			PositionEntity(r\Objects[4], r\x-572.0 * RoomScale, 350.0 * RoomScale, r\z - 4.0 * RoomScale, True)
			
			temp = Rand(1,4)
			PositionEntity(r\Objects[0], EntityX(r\Objects[temp],True),EntityY(r\Objects[temp],True),EntityZ(r\Objects[temp],True),True)
		Case "room2offices3"
			If Rand(2)=1 Then 
				it = CreateItem("Mobile Task Forces", "paper", r\x + 744.0 * RoomScale, r\y +240.0 * RoomScale, r\z + 944.0 * RoomScale)
				EntityParent(it\obj, r\obj)	
			Else
				it = CreateItem("Security Clearance Levels", "paper", r\x + 680.0 * RoomScale, r\y +240.0 * RoomScale, r\z + 944.0 * RoomScale)
				EntityParent(it\obj, r\obj)			
			EndIf
			
			it = CreateItem("Object Classes", "paper", r\x + 160.0 * RoomScale, r\y +240.0 * RoomScale, r\z + 568.0 * RoomScale)
			EntityParent(it\obj, r\obj)	
			
			it = CreateItem("Document", "paper", r\x -1440.0 * RoomScale, r\y +624.0 * RoomScale, r\z + 152.0 * RoomScale)
			EntityParent(it\obj, r\obj)	
			
			it = CreateItem("Radio Transceiver", "radio", r\x - 1184.0 * RoomScale, r\y + 480.0 * RoomScale, r\z - 800.0 * RoomScale)
			EntityParent(it\obj, r\obj)				
			
			For i = 0 To Rand(0,1)
				it = CreateItem("ReVision Eyedrops", "eyedrops", r\x - 1529.0*RoomScale, r\y + 563.0 * RoomScale, r\z - 572.0*RoomScale + i*0.05)
				EntityParent(it\obj, r\obj)				
			Next
			
			it = CreateItem("9V Battery", "bat", r\x - 1545.0 * RoomScale, r\y + 603.0 * RoomScale, r\z - 372.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			If Rand(2) = 1 Then
				it = CreateItem("9V Battery", "bat", r\x - 1540.0 * RoomScale, r\y + 603.0 * RoomScale, r\z - 340.0 * RoomScale)
				EntityParent(it\obj, r\obj)
			EndIf
			If Rand(2) = 1 Then
				it = CreateItem("9V Battery", "bat", r\x - 1529.0 * RoomScale, r\y + 603.0 * RoomScale, r\z - 308.0 * RoomScale)
				EntityParent(it\obj, r\obj)
			EndIf
			
			r\RoomDoors[0] = CreateDoor(r\level, r\x - 1056.0 * RoomScale, 384.0*RoomScale, r\z + 290.0 * RoomScale, 90, r, True)
			r\RoomDoors[0]\AutoClose = False : r\RoomDoors[0]\open = True
			PositionEntity r\RoomDoors[0]\buttons[0], EntityX(r\RoomDoors[0]\buttons[0],True),EntityY(r\RoomDoors[0]\buttons[0],True),r\z + 161.0 * RoomScale,True
			PositionEntity r\RoomDoors[0]\buttons[1], EntityX(r\RoomDoors[0]\buttons[1],True),EntityY(r\RoomDoors[0]\buttons[1],True),r\z + 161.0 * RoomScale,True
			
		Case "start"
			r\RoomDoors[1] = CreateDoor(r\level, r\x + 288.0 * RoomScale, 0, r\z + 384.0 * RoomScale, 90, r, True, True)
			r\RoomDoors[1]\locked = False  :r\RoomDoors[1]\AutoClose = False
			r\RoomDoors[1]\dir = 1 : r\RoomDoors[1]\open = True 
			
			FreeEntity(r\RoomDoors[1]\buttons[0]) : r\RoomDoors[1]\buttons[0] = 0
			FreeEntity(r\RoomDoors[1]\buttons[1]) : r\RoomDoors[1]\buttons[1] = 0
			
			r\RoomDoors[2] = CreateDoor(r\level, r\x - 1008.0 * RoomScale, 0, r\z - 688.0 * RoomScale, 90, r, True)
			r\RoomDoors[2]\AutoClose = False : r\RoomDoors[2]\open = False : r\RoomDoors[2]\locked = True
			FreeEntity(r\RoomDoors[2]\buttons[0]) : r\RoomDoors[2]\buttons[0] = 0
			FreeEntity(r\RoomDoors[2]\buttons[1]) : r\RoomDoors[2]\buttons[1] = 0
			
		Case "room2scps"
			r\RoomDoors[0] = CreateDoor(r\level, r\x + 264.0 * RoomScale, 0, r\z, 90, r, True, False, 3)
			r\RoomDoors[0]\AutoClose = False : r\RoomDoors[0]\open = False
			PositionEntity(r\RoomDoors[0]\buttons[0], r\x + 320.0 * RoomScale, EntityY(r\RoomDoors[0]\buttons[0],True), EntityZ(r\RoomDoors[0]\buttons[0],True), True)
			PositionEntity(r\RoomDoors[0]\buttons[1], r\x + 224.0 * RoomScale, EntityY(r\RoomDoors[0]\buttons[1],True), EntityZ(r\RoomDoors[0]\buttons[1],True), True)
			
			r\RoomDoors[1] = CreateDoor(r\level, r\x + 560.0 * RoomScale, 0, r\z + 272.0 * RoomScale, 0, r, True, False, 4)
			r\RoomDoors[1]\AutoClose = False : r\RoomDoors[1]\open = False
			
			r\RoomDoors[2] = CreateDoor(r\level, r\x + 560.0 * RoomScale, 0, r\z - 272.0 * RoomScale, 180, r, True, False, 3)
			r\RoomDoors[2]\AutoClose = False : r\RoomDoors[2]\open = False : r\RoomDoors[2]\locked = True
			
			it = CreateItem("SCP-714", "scp714", r\x + 552.0 * RoomScale, r\y + 220.0 * RoomScale, r\z + 760.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			it = CreateItem("SCP-1025", "scp1025", r\x + 552.0 * RoomScale, r\y + 224.0 * RoomScale, r\z -758.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			sc.SecurityCams = CreateSecurityCam(r\x + 560.0 * RoomScale, r\y + 386 * RoomScale, r\z + 416.0 * RoomScale, r)
			sc\angle = 0 : sc\turn = 0
			TurnEntity(sc\CameraObj, 35, 0, 0)
			EntityParent(sc\obj, r\obj)
			
			sc.SecurityCams = CreateSecurityCam(r\x + 560.0 * RoomScale, r\y + 386 * RoomScale, r\z - 416.0 * RoomScale, r)
			sc\angle = 180 : sc\turn = 0
			TurnEntity(sc\CameraObj, 35, 0, 0)
			EntityParent(sc\obj, r\obj)
			
			it = CreateItem("Document SCP-714", "paper", r\x + 728.0 * RoomScale, r\y + 288.0 * RoomScale, r\z + 360.0 * RoomScale)
			EntityParent(it\obj, r\obj)	
			
		Case "coffin"
			d = CreateDoor(r\level, r\x, 0, r\z - 448.0 * RoomScale, 0, r, False, True, 2)
			d\dir = 1 : d\AutoClose = False : d\open = False
			PositionEntity(d\buttons[0], r\x - 384.0 * RoomScale, 0.7, r\z - 280.0 * RoomScale, True)
			
			sc.SecurityCams = CreateSecurityCam(r\x - 320.0 * RoomScale, r\y + 704 * RoomScale, r\z + 288.0 * RoomScale, r, True)
			sc\angle = 45 + 180
			sc\turn = 45
			sc\Cursed = True
			TurnEntity(sc\CameraObj, 120, 0, 0)
			EntityParent(sc\obj, r\obj)				
			
			PositionEntity(sc\ScrObj, r\x - 800 * RoomScale, 288.0 * RoomScale, r\z - 350.0 * RoomScale)
			EntityParent(sc\ScrObj, r\obj)
			TurnEntity(sc\ScrObj, 0, 180, 0)
			
			r\Objects[0] = CreatePivot()
			PositionEntity(r\Objects[0], r\x, -1320.0 * RoomScale, r\z + 2304.0 * RoomScale)
			EntityParent(r\Objects[0], r\obj)
			
			it = CreateItem("Document SCP-895", "paper", r\x - 688.0 * RoomScale, r\y + 133.0 * RoomScale, r\z - 304.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			r\Objects[1] = CreatePivot(r\obj)
			PositionEntity(r\Objects[1], r\x + 96.0*RoomScale, -1535.0 * RoomScale, r\z + 32.0 * RoomScale,True)
			
			de.Decals = CreateDecal(0, r\x + 96.0*RoomScale, -1535.0 * RoomScale, r\z + 32.0 * RoomScale, 90, Rand(360), 0)
			EntityParent de\obj, r\obj
			
		Case "room2tesla"
			r\Objects[0] = CreatePivot()
			PositionEntity(r\Objects[0], r\x - 114.0 * RoomScale, 0.0, r\z)
			EntityParent(r\Objects[0], r\obj)
			
			r\Objects[1] = CreatePivot()
			PositionEntity(r\Objects[1], r\x + 114.0 * RoomScale, 0.0, r\z)
			EntityParent(r\Objects[1], r\obj)			
			
			r\Objects[2] = CreatePivot()
			PositionEntity(r\Objects[2], r\x, 0.0, r\z)
			EntityParent(r\Objects[2], r\obj)	
			
			r\Objects[3] = CreateSprite()
			EntityTexture (r\Objects[3], TeslaTexture)
			SpriteViewMode(r\Objects[3],2) 
			;ScaleSprite (r\Objects[3],((512.0 * RoomScale)/2.0),((512.0 * RoomScale)/2.0))
			EntityBlend (r\Objects[3], blend_add) 
			EntityFX(r\Objects[3], 1 + 8 + 16)
			
			PositionEntity(r\Objects[3], r\x, 0.8, r\z)
			
			HideEntity r\Objects[3]
			EntityParent(r\Objects[3], r\obj)
			
			w.waypoints = CreateWaypoint(r\x, r\y + 66.0 * RoomScale, r\z + 292.0 * RoomScale, Null, r)
			w2.waypoints = CreateWaypoint(r\x, r\y + 66.0 * RoomScale, r\z - 284.0 * RoomScale, Null, r)
			w\connected[0] = w2 : w\dist[0] = EntityDistance(w\obj, w2\obj)
			w2\connected[0] = w : w2\dist[0] = w\dist[0]
			
		Case "room2doors"
			d = CreateDoor(r\level, r\x, 0, r\z + 528.0 * RoomScale, 0, r, True)
			d\AutoClose = False ;: d\buttons[0] = False
			PositionEntity (d\buttons[0], r\x - 832.0 * RoomScale, 0.7, r\z + 160.0 * RoomScale, True)
			PositionEntity (d\buttons[1], r\x - 224.0 * RoomScale, 0.7, r\z + 896.0 * RoomScale, True)
			RotateEntity(d\buttons[1], 0, 90, 0, True)
			
			d2 = CreateDoor(r\level, r\x, 0, r\z - 528.0 * RoomScale, 180, r, True)
			d2\AutoClose = False : FreeEntity (d2\buttons[0]) : d2\buttons[0] = 0
			PositionEntity (d2\buttons[1], r\x - 224.0 * RoomScale, 0.7, r\z - 896.0 * RoomScale, True)
			RotateEntity(d2\buttons[1], 0, 90, 0, True)
			
			r\Objects[0] = CreatePivot()
			PositionEntity(r\Objects[0], r\x - 832.0 * RoomScale, 0.5, r\z)
			EntityParent(r\Objects[0], r\obj)
			
			d2\LinkedDoor = d : d\LinkedDoor = d2
			
			d\open = False : d2\open = True
			
		Case "914"
			d = CreateDoor(r\level, r\x, 0, r\z - 368.0 * RoomScale, 0, r, False, True, 2)
			d\dir = 1 : d\AutoClose = False : d\open = False
			PositionEntity (d\buttons[0], r\x - 496.0 * RoomScale, 0.7, r\z - 272.0 * RoomScale, True)
			TurnEntity(d\buttons[0], 0, 90, 0)
			
			r\Objects[0] = LoadMesh("GFX\map\914key.x")
			r\Objects[1] = LoadMesh("GFX\map\914knob.x")
			
			For  i% = 0 To 1
				ScaleEntity(r\Objects[i], RoomScale, RoomScale, RoomScale)
				EntityPickMode(r\Objects[i], 2)
			Next
			
			PositionEntity (r\Objects[0], r\x, r\y + 190.0 * RoomScale, r\z + 374.0 * RoomScale)
			PositionEntity (r\Objects[1], r\x, r\y + 230.0 * RoomScale, r\z + 374.0 * RoomScale)
			EntityParent(r\Objects[0], r\obj)
			EntityParent(r\Objects[1], r\obj)
			
			d = CreateDoor(r\level, r\x - 624.0 * RoomScale, 0.0,r\z + 528.0 * RoomScale, 180, r, True)
			FreeEntity (d\obj2) : d\obj2 = 0
			FreeEntity (d\buttons[0]) : d\buttons[0] = 0
			FreeEntity (d\buttons[1]) : d\buttons[1] = 0
			r\RoomDoors[0] = d: d\AutoClose = False
			
			d = CreateDoor(r\level, r\x + 816.0 * RoomScale, 0.0, r\z + 528.0 * RoomScale, 180, r, True)
			FreeEntity (d\obj2) : d\obj2 = 0	
			FreeEntity (d\buttons[0]) : d\buttons[0] = 0
			FreeEntity (d\buttons[1]) : d\buttons[1] = 0
			r\RoomDoors[1] = d : d\AutoClose = False
			
			r\Objects[2] = CreatePivot()
			r\Objects[3] = CreatePivot()
			PositionEntity(r\Objects[2], r\x - 712.0 * RoomScale, 0.5, r\z + 640.0 * RoomScale)
			PositionEntity(r\Objects[3], r\x + 728.0 * RoomScale, 0.5, r\z + 640.0 * RoomScale)
			EntityParent(r\Objects[2], r\obj)
			EntityParent(r\Objects[3], r\obj)
			
			it = CreateItem("Note", "paper", r\x +954.0 * RoomScale, r\y +228.0 * RoomScale, r\z + 127.0 * RoomScale)
			EntityParent(it\obj, r\obj)	
			
			it = CreateItem("First Aid Kit", "firstaid", r\x + 960.0 * RoomScale, r\y + 112.0 * RoomScale, r\z - 40.0 * RoomScale)
			EntityParent(it\obj, r\obj) : RotateEntity(it\obj, 0, 90, 0)
			
		Case "173"
			r\Objects[0] = CreatePivot()
			PositionEntity (r\Objects[0], EntityX(r\obj) + 40.0 * RoomScale, 430.0 * RoomScale, EntityZ(r\obj) + 1052.0 * RoomScale)
			r\Objects[1] = CreatePivot()
			PositionEntity (r\Objects[1], EntityX(r\obj) - 80.0 * RoomScale, 100.0 * RoomScale, EntityZ(r\obj) + 526.0 * RoomScale)
			r\Objects[2] = CreatePivot()
			PositionEntity (r\Objects[2], EntityX(r\obj) - 128.0 * RoomScale, 100.0 * RoomScale, EntityZ(r\obj) + 320.0 * RoomScale)
			
			r\Objects[3] = CreatePivot()
			PositionEntity (r\Objects[3], EntityX(r\obj) + 660.0 * RoomScale, 100.0 * RoomScale, EntityZ(r\obj) + 526.0 * RoomScale)
			r\Objects[4] = CreatePivot()
			PositionEntity (r\Objects[4], EntityX(r\obj) + 700 * RoomScale, 100.0 * RoomScale, EntityZ(r\obj) + 320.0 * RoomScale)
			
			r\Objects[5] = CreatePivot()
			PositionEntity (r\Objects[5], EntityX(r\obj) + 1472.0 * RoomScale, 100.0 * RoomScale, EntityZ(r\obj) + 912.0 * RoomScale)
			
			For i = 0 To 5
				EntityParent(r\Objects[i], r\obj)
			Next
			
			r\RoomDoors[0] = CreateDoor(r\level, EntityX(r\obj), 0, EntityZ(r\obj) - 4.0, 0, r, False)
			r\RoomDoors[0]\locked = True
			r\RoomDoors[0]\AutoClose = False : r\RoomDoors[0]\open = False
			
			r\RoomDoors[1] = CreateDoor(r\level, EntityX(r\obj) + 288.0 * RoomScale, 0, EntityZ(r\obj) + 384.0 * RoomScale, 90, r, False, True)
			r\RoomDoors[1]\AutoClose = False ;: r\RoomDoors[1]\locked = True
			r\RoomDoors[1]\dir = 1 : r\RoomDoors[1]\open = False
			
			FreeEntity(r\RoomDoors[1]\buttons[0]) : r\RoomDoors[1]\buttons[0] = 0
			FreeEntity(r\RoomDoors[1]\buttons[1]) : r\RoomDoors[1]\buttons[1] = 0
			
			de.Decals = CreateDecal(Rand(4, 5), EntityX(r\Objects[5], True), 0.002, EntityZ(r\Objects[5], True), 90, Rnd(360), 0)
			de\Size = 1.2
			ScaleSprite(de\obj, de\Size, de\Size)
			
			For xtemp% = 0 To 1
				For ztemp% = 0 To 1
					de.Decals = CreateDecal(Rand(4, 6), r\x + 700.0 * RoomScale + xtemp * 700.0 * RoomScale + Rnd(-0.5, 0.5), Rnd(0.001, 0.0018), r\z + 600 * ztemp * RoomScale + Rnd(-0.5, 0.5), 90, Rnd(360), 0)
					de\Size = Rnd(0.5, 0.8)
					de\Alpha = Rnd(0.8, 1.0)
					ScaleSprite(de\obj, de\Size, de\Size)
				Next
			Next
			
			AddLight(r, r\x-224.0*RoomScale, r\y+640.0*RoomScale, r\z+128.0*RoomScale,2,2,200,200,200)
			AddLight(r, r\x-1056.0*RoomScale, r\y+608.0*RoomScale, r\z+416.0*RoomScale,2,2,200,200,200)
			
			r\RoomDoors[2] = CreateDoor(r\level, r\x - 1008.0 * RoomScale, 0, r\z - 688.0 * RoomScale, 90, r, True)
			r\RoomDoors[2]\AutoClose = False : r\RoomDoors[2]\open = False : r\RoomDoors[2]\locked = True
			FreeEntity(r\RoomDoors[2]\buttons[0]) : r\RoomDoors[2]\buttons[0] = 0
			FreeEntity(r\RoomDoors[2]\buttons[1]) : r\RoomDoors[2]\buttons[1] = 0
			
			r\RoomDoors[3] = CreateDoor(r\level, r\x - 2320.0 * RoomScale, 0, r\z - 1248.0 * RoomScale, 90, r, True)
			r\RoomDoors[3]\AutoClose = False : r\RoomDoors[3]\open = True : r\RoomDoors[3]\locked = True
			
			r\RoomDoors[4] = CreateDoor(r\level, r\x - 4352.0 * RoomScale, 0, r\z - 1248.0 * RoomScale, 90, r, True)
			r\RoomDoors[4]\AutoClose = False : r\RoomDoors[4]\open = True : r\RoomDoors[4]\locked = True	
			
			r\RoomDoors[5] = CreateDoor(r\level, r\x - 5760.0 * RoomScale, 0, r\z + 320.0 * RoomScale, 0, r, True)
			r\RoomDoors[5]\AutoClose = False : r\RoomDoors[5]\open = True : r\RoomDoors[5]\locked = True	
			
			;the door in the office below the walkway
			r\RoomDoors[7] = CreateDoor(r\level, r\x - 3712.0 * RoomScale, -385*RoomScale, r\z - 128.0 * RoomScale, 0, r, True)
			r\RoomDoors[7]\AutoClose = False : r\RoomDoors[7]\open = True
			
			d.Doors = CreateDoor(r\level, r\x - 5760 * RoomScale, 0, r\z + 1216 * RoomScale, 0, r, False)
			d\locked = True
			d\DisableWaypoint = True
			
			tex = LoadTexture("GFX\map\Door02.jpg")
			For ztemp = 0 To 1			
				For xtemp = 0 To 2
					d.Doors = CreateDoor(r\level, r\x - (7424.0-512.0*xtemp) * RoomScale, 0, r\z + (1008.0-480.0*ztemp) * RoomScale, 180*(Not ztemp), r, False)
					EntityTexture d\obj, tex
					d\locked = True
					FreeEntity d\obj2 : d\obj2=0
					FreeEntity d\buttons[0] : d\buttons[0]=0
					FreeEntity d\buttons[1] : d\buttons[1]=0
					d\DisableWaypoint = True
				Next					
				For xtemp = 0 To 6
					d.Doors = CreateDoor(r\level, r\x - (5120.0-512.0*xtemp) * RoomScale, 0, r\z + (1008.0-480.0*ztemp) * RoomScale, 180*(Not ztemp), r, False)
					EntityTexture d\obj, tex
					d\locked = True
					FreeEntity d\obj2 : d\obj2=0
					FreeEntity d\buttons[0] : d\buttons[0]=0
					FreeEntity d\buttons[1] : d\buttons[1]=0	
					d\DisableWaypoint = True
					
					If xtemp = 4 And ztemp = 1 Then r\RoomDoors[6] = d
				Next	
			Next
			
			CreateItem("Class D Orientation Leaflet", "paper", r\x-2914*RoomScale, 170.0*RoomScale, r\z+40*RoomScale)
			
			;For i = 0 To 4
				;	d:Decals = createdecal(Rand(4, 6), x + Rnd(400, 1712) * RoomScale, Rnd(0.001, 0.0018), z + Rnd(-144, 912) * RoomScale, 90, Rnd(360), 0)
				;	d\size = Rnd(0.5, 0.8)
				;	d\alpha = Rnd(0.8, 1.0)
				;	scaleSprite(d\obj, d\size, d\size)
			;Next
		Case "room2ccont"
			d = CreateDoor(r\level, r\x + 64.0 * RoomScale, 0.0, r\z + 368.0 * RoomScale, 180, r, False, False, 1)
			d\AutoClose = False : d\open = False
			
			it = CreateItem("Note from Daniel", "paper", r\x-400.0*RoomScale,1040.0*RoomScale,r\z+115.0*RoomScale)
			EntityParent(it\obj, r\obj)
			
			For n% = 0 To 2
				r\Objects[n * 2] = CopyEntity(LeverBaseOBJ)
				r\Objects[n * 2 + 1] = CopyEntity(LeverOBJ)
				
				For  i% = 0 To 1
					ScaleEntity(r\Objects[n * 2 + i], 0.04, 0.04, 0.04)
					PositionEntity (r\Objects[n * 2 + i], r\x - 240.0 * RoomScale, r\y + 1104.0 * RoomScale, r\z + (632.0 - 64.0 * n) * RoomScale, True)
					
					EntityParent(r\Objects[n * 2 + i], r\obj)
				Next
				RotateEntity(r\Objects[n * 2], 0, -90, 0)
				RotateEntity(r\Objects[n * 2 + 1], 10, -90 - 180, 0)
				
				;EntityPickMode(r\Objects[n * 2 + 1], 2)
				EntityPickMode r\Objects[n * 2 + 1], 1, False
				EntityRadius r\Objects[n * 2 + 1], 0.1
				;makecollbox(r\Objects[n * 2 + 1])
			Next
		Case "room106"
			
			it = CreateItem("Level 4 Key Card", "key4", r\x - 752.0 * RoomScale, r\y - 592 * RoomScale, r\z + 3026.0 * RoomScale)
			EntityParent(it\obj, r\obj)
			
			d = CreateDoor(r\level, r\x - 968.0 * RoomScale, -764.0 * RoomScale, r\z + 1392.0 * RoomScale, 0, r, False, False, 3)
			d\AutoClose = False : d\open = False	
			
			d = CreateDoor(r\level, r\x, 0, r\z - 464.0 * RoomScale, 0, r, False, False, 3)
			d\AutoClose = False : d\open = False			
			
			d = CreateDoor(r\level, r\x - 624.0 * RoomScale, -1280.0 * RoomScale, r\z, 90, r, False, False, 4)
			d\AutoClose = False : d\open = False	
			
			r\Objects[6] = LoadMesh("GFX\map\room1062.b3d")
			
			ScaleEntity (r\Objects[6],RoomScale,RoomScale,RoomScale)
			EntityType r\Objects[6], HIT_MAP
			EntityPickMode r\Objects[6], 3
			PositionEntity(r\Objects[6],r\x+784.0*RoomScale,-980.0*RoomScale,r\z+720.0*RoomScale,True)
			
			If BumpEnabled Then 
				
				For i = 1 To CountSurfaces(r\Objects[6])
					sf = GetSurface(r\Objects[6],i)
					b = GetSurfaceBrush( sf )
					t = GetBrushTexture(b,1)
					texname$ =  StripPath(TextureName(t))
					
					For mat.materials = Each Materials
						If texname = mat\name Then
							
							t1 = GetBrushTexture(b,0)
							t2 = GetBrushTexture(b,1)
							;bump = mat\bump
							
							BrushTexture b, t1, 0, 0	
							BrushTexture b, mat\bump, 0, 1
							BrushTexture b, t2, 0, 2					
							
							PaintSurface sf,b
							
							Exit
						EndIf 
					Next
				Next
				
			EndIf
			
			EntityParent(r\Objects[6], r\obj)
			
			For n = 0 To 2 Step 2
				r\Objects[n] = CopyEntity(LeverBaseOBJ)
				r\Objects[n+1] = CopyEntity(LeverOBJ)
				
				For i% = 0 To 1
					ScaleEntity(r\Objects[n+i], 0.04, 0.04, 0.04)
					PositionEntity (r\Objects[n+i], r\x - (555.0 - 81.0 * (n/2)) * RoomScale, r\y - 576.0 * RoomScale, r\z + 3040.0 * RoomScale, True)
					
					EntityParent(r\Objects[n+i], r\obj)
				Next
				RotateEntity(r\Objects[n], 0, 0, 0)
				RotateEntity(r\Objects[n+1], 10, -180, 0)
				
				;EntityPickMode(r\Objects[n * 2 + 1], 2)
				EntityPickMode r\Objects[n+1], 1, False
				EntityRadius r\Objects[n+1], 0.1
				;makecollbox(r\Objects[n * 2 + 1])
			Next
			
			RotateEntity(r\Objects[1], 81,-180,0)
			RotateEntity(r\Objects[3], -81,-180,0)			
			
			r\Objects[4] = CreateButton(r\x - 146.0*RoomScale, r\y - 576.0 * RoomScale, r\z + 3045.0 * RoomScale, 0,0,0)
			EntityParent (r\Objects[4],r\obj)

			sc.SecurityCams = CreateSecurityCam(r\x + 768.0 * RoomScale, r\y + 1392.0 * RoomScale, r\z + 1696.0 * RoomScale, r, True)
			sc\angle = 45 + 90 + 180
			sc\turn = 20
			TurnEntity(sc\CameraObj, 45, 0, 0)
			EntityParent(sc\obj, r\obj)
			
			r\Objects[7] = sc\CameraObj
			r\Objects[8] = sc\obj
			
			PositionEntity(sc\ScrObj, r\x - 272.0 * RoomScale, -544.0 * RoomScale, r\z + 3020.0 * RoomScale)
			TurnEntity(sc\ScrObj, 0, -10, 0)
			EntityParent(sc\ScrObj, r\obj)
			
			r\NPC[0] = CreateNPC(NPCtypeD, r\x + 1088.0 * RoomScale, 1096.0 * RoomScale, r\z + 1728.0 * RoomScale)
			r\Objects[5] = CreatePivot()
			TurnEntity r\Objects[5], 0,180,0
			PositionEntity (r\Objects[5], r\x + 1088.0 * RoomScale, 1104.0 * RoomScale, r\z + 1888.0 * RoomScale) 
			EntityParent r\Objects[5], r\obj
			HideEntity r\NPC[0]\obj
			
			r\Objects[9] = CreatePivot(r\obj)
			PositionEntity (r\Objects[9], r\x - 272 * RoomScale, r\y - 672.0 * RoomScale, r\z + 2736.0 * RoomScale, True)
			
		Case "pocketdimension"
			
			Local hallway = LoadMesh("GFX\map\pocketdimension2.b3d")
			r\Objects[8]=LoadMesh("GFX\map\pocketdimension3.b3d")	
			r\Objects[9]=LoadMesh("GFX\map\pocketdimension4.b3d")		
			r\Objects[10]=CopyMesh(r\Objects[9])
			
			r\Objects[11]=LoadMesh("GFX\map\pocketdimension5.b3d")
			
			;ScaleEntity hallway, RoomScale,RoomScale,RoomScale
			
			CreateItem("Burnt Note", "paper", EntityX(r\obj),0.5,EntityZ(r\obj)+3.5)
			
			For n = 0 To 4
				
				Select n
					Case 0
						entity = hallway 					
					Case 1
						entity = r\Objects[8]						
					Case 2
						entity = r\Objects[9]						
					Case 3
						entity = r\Objects[10]							
					Case 4
						entity = r\Objects[11]							
				End Select 
				
				If BumpEnabled Then 
				
					For i = 1 To CountSurfaces(entity)
						sf = GetSurface(entity,i)
						b = GetSurfaceBrush( sf )
						t = GetBrushTexture(b,1)
						texname$ =  StripPath(TextureName(t))
						
						For mat.materials = Each Materials
							If texname = mat\name Then
								
								t1 = GetBrushTexture(b,0)
								t2 = GetBrushTexture(b,1)
								;bump = mat\bump
								
								BrushTexture b, t1, 0, 0	
								BrushTexture b, mat\bump, 0, 1
								BrushTexture b, t2, 0, 2					
								
								PaintSurface sf,b
								
								Exit
							EndIf 
						Next
					Next
					
				EndIf
				
			Next
			
			For i = 8 To 11
				ScaleEntity (r\Objects[i],RoomScale,RoomScale,RoomScale)
				EntityType r\Objects[i], HIT_MAP
				EntityPickMode r\Objects[i], 3
				PositionEntity(r\Objects[i],r\x,r\y,r\z+32.0,True)
			Next
			
			ScaleEntity (r\Objects[10],RoomScale*1.5,RoomScale*2.0,RoomScale*1.5,True)			
			PositionEntity(r\Objects[11],r\x,r\y,r\z+64.0,True)			
			
			For i = 1 To 8
				r\Objects[i-1] = CopyMesh(hallway)
				ScaleEntity (r\Objects[i-1],RoomScale,RoomScale,RoomScale)
				angle# = (i-1) * (360.0/8.0)
				
				EntityType r\Objects[i-1], HIT_MAP
				EntityPickMode r\Objects[i-1], 3		
				
				RotateEntity(r\Objects[i-1],0,angle-90,0)
				PositionEntity(r\Objects[i-1],r\x+Cos(angle)*(512.0*RoomScale),0.0,r\z+Sin(angle)*(512.0*RoomScale))
				EntityParent (r\Objects[i-1], r\obj)
				
				If i < 6 Then 
					de.Decals = CreateDecal(i+7, r\x+Cos(angle)*(512.0*RoomScale)*3.0, 0.02,r\z+Sin(angle)*(512.0*RoomScale)*3.0, 90,angle-90,0)
					de\Size = Rnd(0.5, 0.5)
					de\blendmode = 2
					de\fx = 1+8
					ScaleSprite(de\obj, de\Size, de\Size)
					EntityFX(de\obj, 1+8)
					EntityBlend de\obj, 2
				EndIf				
			Next
			
			For i = 12 To 16
				r\Objects[i] = CreatePivot(r\Objects[11])
				Select i
					Case 12
						PositionEntity(r\Objects[i],r\x,r\y+200*RoomScale,r\z+64.0,True)	
					Case 13
						PositionEntity(r\Objects[i],r\x+390*RoomScale,r\y+200*RoomScale,r\z+64.0+272*RoomScale,True)	
					Case 14
						PositionEntity(r\Objects[i],r\x+838*RoomScale,r\y+200*RoomScale,r\z+64.0-551*RoomScale,True)	
					Case 15
						PositionEntity(r\Objects[i],r\x-139*RoomScale,r\y+200*RoomScale,r\z+64.0+1201*RoomScale,True)	
					Case 16
						PositionEntity(r\Objects[i],r\x-1238*RoomScale,r\y-1664*RoomScale,r\z+64.0+381*RoomScale,True)
				End Select 
				
			Next
			
			Local OldManEyes% = LoadTexture("GFX\npcs\oldmaneyes.jpg")
			r\Objects[17] = CreateSprite()
			ScaleSprite(r\Objects[17], 0.03, 0.03)
			EntityTexture(r\Objects[17], OldManEyes)
			EntityBlend (r\Objects[17], 3)
			EntityFX(r\Objects[17], 1 + 8)
			SpriteViewMode(r\Objects[17], 2)
			
			FreeTexture t
			FreeEntity hallway
	End Select
	
	For lt.lighttemplates = Each LightTemplates
		If lt\roomtemplate = r\RoomTemplate Then
			newlt = AddLight(r, r\x+lt\x, r\y+lt\y, r\z+lt\z, lt\ltype, lt\range, lt\r, lt\g, lt\b)
			If newlt <> 0 Then 
				DebugLog r\RoomTemplate\Name+" - "+lt\x+", "+lt\y+", "+lt\z+" - "+lt\range+", "+lt\r
				If lt\ltype = 3 Then
					LightConeAngles(newlt, lt\innerconeangle, lt\outerconeangle)
					RotateEntity(newlt, lt\pitch, lt\yaw, 0)
				EndIf
			Else
				DebugLog r\RoomTemplate\Name+" - light error"
			EndIf
		EndIf
	Next
	
	For ts.tempscreens = Each TempScreens
		If ts\roomtemplate = r\RoomTemplate Then
			CreateScreen(r\x+ts\x, r\y+ts\y, r\z+ts\z, ts\imgpath, r)
		EndIf
	Next
	
	For tw.TempWayPoints = Each TempWayPoints
		If tw\roomtemplate = r\RoomTemplate Then
			CreateWaypoint(r\x+tw\x, r\y+tw\y, r\z+tw\z, Null, r)
		EndIf
	Next
	
	For i = 0 To 3
		If r\RoomTemplate\tempsoundemitter[i]<>0 Then
			r\SoundEmitterObj[i]=CreatePivot(r\obj)
			PositionEntity r\SoundEmitterObj[i], r\x+r\RoomTemplate\tempsoundemitterx[i],r\y+r\RoomTemplate\tempsoundemittery[i],r\z+r\RoomTemplate\tempsoundemitterz[i],True
			r\SoundEmitter[i] = r\RoomTemplate\tempSoundEmitter[i]
			r\SoundEmitterRange[i] = r\RoomTemplate\tempSoundEmitterrange[i]
			
			;DebugLog r\RoomTemplate\Name+" - "+EntityX(r\RoomTemplate\tempsoundemitterobj[i],True)+", "+EntityY(r\RoomTemplate\tempsoundemitterobj[i],True)+", "+EntityZ(r\RoomTemplate\tempsoundemitterobj[i],True)
		EndIf
	Next
	
	
	
End Function

Global UpdateRoomTimer#
Function UpdateRooms()
	Local dist#, p.Particles, r.Rooms
	
	TempLightVolume = 0
	For r.Rooms = Each Rooms
		Local hide% = False
		If PlayerRoom = r Then
			r\found = True
		ElseIf Abs(EntityX(Collider)-r\x)>HideDistance 
			hide = True
		ElseIf Abs(EntityZ(Collider)-r\z)>HideDistance 
			hide = True
		Else
			dist# = Distance(EntityX(Collider),EntityZ(Collider),r\x,r\z) 
			If dist > HideDistance Then hide = True
		EndIf
		
		If hide = True Then
			HideEntity(r\obj)
			For i = 0 To 19
				If r\Lights[i] <> 0 Then 
					;dist = EntityDistance(Collider,r\lights[i])
					;If dist > Min(HideDistance*r\lightdist[i],HideDistance) Then HideEntity(r\lights[i])
					HideEntity(r\Lights[i])
				EndIf
			Next
		Else
			ShowEntity r\obj
			For i = 0 To 19
				If r\Lights[i] <> 0 Then
					dist = EntityDistance(Collider,r\Lights[i])
					TempLightVolume = TempLightVolume + r\LightIntensity[i]*r\LightIntensity[i]*((HideDistance-dist)/HideDistance)
					ShowEntity(r\Lights[i]) 
				EndIf
			Next	
		EndIf
	Next
	
	TempLightVolume = Max(TempLightVolume / 5,0.8)
End Function


Global LightVolume#, TempLightVolume#
Function AddLight%(room.Rooms, x#, y#, z#, ltype%, range#, r%, g%, b%)
	For i = 0 To 19
		If room\Lights[i]=0 Then
			room\Lights[i] = CreateLight(ltype)
			;room\LightDist[i] = range
			LightRange(room\Lights[i],range)
			LightColor(room\Lights[i],r,g,b)
			PositionEntity(room\Lights[i],x,y,z,True)
			EntityParent(room\Lights[i],room\obj)
			
			room\LightIntensity[i] = (r+g+b)/255.0/3.0
			
			room\LightSprites[i]= CreateSprite()
			PositionEntity(room\LightSprites[i], x, y, z)
			ScaleSprite(room\LightSprites[i], 0.13 , 0.13)
			EntityTexture(room\LightSprites[i], LightSpriteTex(0))
			EntityBlend (room\LightSprites[i], 3)
			
			EntityParent(room\LightSprites[i], room\obj)
			
			Return room\Lights[i]
		EndIf
	Next
End Function

;-------------------------------------------------------------------------------------------------------

Type TempWayPoints
	Field x#, y#, z#
	Field roomtemplate.RoomTemplates
End Type 

Type WayPoints
	Field obj
	Field door.Doors
	Field room.Rooms
	Field state%
	;Field tempDist#
	;Field tempSteps%
	Field connected.WayPoints[5]
	Field dist#[5]
	
	Field Fcost#, Gcost#, Hcost#
	
	Field parent.WayPoints
End Type

Function CreateWaypoint.WayPoints(x#,y#,z#,door.Doors, room.Rooms)
	
	w.waypoints = New WayPoints
	
	If 1 Then
		w\obj = CreatePivot()
		PositionEntity w\obj, x,y,z	
	Else
		w\obj = CreateSprite()
		PositionEntity(w\obj, x, y, z)
		ScaleSprite(w\obj, 0.15 , 0.15)
		EntityTexture(w\obj, LightSpriteTex(0))
		EntityBlend (w\obj, 3)	
	EndIf
		
	EntityParent w\obj, room\obj
	
	w\room = room
	w\door=door
	
	Return w
End Function

Function InitWayPoints(loadingstart=60)
	
	Local d.Doors, w.WayPoints, w2.WayPoints, r.Rooms, ClosestRoom.Rooms
	
	Local x#, y#, z#
	
	temper = MilliSecs()
	
	Local dist#, dist2#
	
	For d.Doors = Each Doors
		If d\obj <> 0 Then HideEntity d\obj
		If d\obj2 <> 0 Then HideEntity d\obj2	
		If d\frameobj <> 0 Then HideEntity d\frameobj
		
		If d\room = Null Then 
			ClosestRoom.Rooms = Null
			dist# = 30
			For r.Rooms = Each Rooms
				x# = Abs(EntityX(r\obj,True)-EntityX(d\frameobj,True))
				If x < 20.0 Then
					z# = Abs(EntityZ(r\obj,True)-EntityZ(d\frameobj,True))
					If z < 20.0 Then
						dist2 = x*x+z*z
						If dist2 < dist Then
							ClosestRoom = r
							dist = dist2
						EndIf
					EndIf
				EndIf
			Next
		Else
			ClosestRoom = d\room
		EndIf
		
		If (Not d\DisableWaypoint) Then CreateWaypoint(EntityX(d\frameobj, True), EntityY(d\frameobj, True)+0.18, EntityZ(d\frameobj, True), d, ClosestRoom)
	Next
	
	amount# = 0
	For w.WayPoints = Each WayPoints
		EntityPickMode w\obj, 1, True
		EntityRadius w\obj, 0.2
		amount=amount+1
	Next
	
	
	pvt = CreatePivot()
	
	number = 0
	iter = 0
	For w.WayPoints = Each WayPoints
		
		number = number + 1
		iter = iter + 1
		If iter = 20 Then 
			DrawLoading(loadingstart+Floor((19.0/amount)*number)) 
			iter = 0
		EndIf
		
		w2.WayPoints = After(w)
		
		While (w2<>Null)
			x# = Abs(EntityX(w2\obj,True)-EntityX(w\obj,True))
			If x < 8.0 Then
				z# = Abs(EntityZ(w2\obj,True)-EntityZ(w\obj,True))
				If z < 8.0 Then
					y# = Abs(EntityY(w2\obj,True)-EntityY(w\obj,True))
					If y < 7.0 Then 
						
						dist# = Sqr(x*x+y*y+z*z)
						
						If dist < 7.0 Then
							;PositionEntity pvt, EntityX(w\obj,True),EntityY(w\obj,True),EntityZ(w\obj,True)
							;PointEntity pvt, w2\obj
							;HideEntity w\obj
							
							;LinePick(EntityX(w\obj,True),EntityY(w\obj,True),EntityZ(w\obj,True),x,y,z)
							;EntityPick(pvt, dist);*1.2)
							;e=PickedEntity()
							;If e<>w2\obj Then
							;	DebugLog w\room\RoomTemplate\Name+" - "+e
							;	DebugLog x+", "+y+", "+z
							;EndIf
							
							If EntityVisible(w\obj, w2\obj) Then;e=w2\obj Then 
								;dist = dist
								For i = 0 To 4
									If w\connected[i] = Null Then
										w\connected[i] = w2.WayPoints 
										w\dist[i] = dist
										Exit
									EndIf
								Next
								
								For n = 0 To 4
									If w2\connected[n] = Null Then 
										w2\connected[n] = w.WayPoints 
										w2\dist[n] = dist
										Exit
									EndIf					
								Next
							EndIf
							;ShowEntity w\obj
							
						EndIf	
							
						
					EndIf
				EndIf
			EndIf
			w2 = After(w2)
		Wend
		
	Next
	
	FreeEntity pvt	
	
	For d.Doors = Each Doors
		If d\obj <> 0 Then ShowEntity d\obj
		If d\obj2 <> 0 Then ShowEntity d\obj2	
		If d\frameobj <> 0 Then ShowEntity d\frameobj		
	Next
	
	For w.WayPoints = Each WayPoints
		EntityPickMode w\obj, 0, 0
		EntityRadius w\obj, 0
		
		For i = 0 To 4
			If w\connected[i]<>Null Then 
				tline = CreateLine(EntityX(w\obj,True),EntityY(w\obj,True),EntityZ(w\obj,True),EntityX(w\connected[i]\obj,True),EntityY(w\connected[i]\obj,True),EntityZ(w\connected[i]\obj,True))
				EntityColor(tline, 255,0,0)
				EntityParent tline, w\obj
			EndIf
		Next
	Next
	
	DebugLog "InitWaypoints() - "+(MilliSecs()-temper)
	
End Function

Function FindPath(n.NPCs, x#, y#, z#)
	
	;pathstatus = 0, ei ole etsitty reittiä
	;pathstatus = 1, reitti löydetty
	;pathstatus = 2, reittiä ei ole olemassa
	
	Local temp%, dist#, dist2#
	Local xtemp#, ytemp#, ztemp#
	
	Local w.WayPoints, StartPoint.WayPoints, EndPoint.WayPoints	
	
	For w.WayPoints = Each WayPoints 
		w\state = 0
		w\Fcost = 0
		w\Gcost = 0
		w\Hcost = 0
	Next	
	
	n\PathStatus = 0
	n\PathLocation = 0
	For i = 0 To 19
		n\Path[i] = Null
	Next
	
	temp = CreatePivot()
	PositionEntity(temp, EntityX(n\Collider,True), EntityY(n\Collider,True)+0.15, EntityZ(n\Collider,True))
	
	;käytetään aloituspisteenä waypointia, joka on lähimpänä loppupistettä ja joka on näkyvissä
	Local pvt = CreatePivot()
	PositionEntity(pvt, x,y,z, True)
	
	dist = 100.0
	For w.WayPoints = Each WayPoints
		xtemp = Abs(EntityX(w\obj,True)-EntityX(temp,True))
		If xtemp < 8.0 Then 
			ztemp = Abs(EntityZ(w\obj,True)-EntityZ(temp,True))
			If ztemp < 8.0 Then 
				ytemp = Abs(EntityY(w\obj,True)-EntityY(temp,True))
				If ytemp < 8.0 Then 
					dist2# = xtemp+ztemp+ytemp ;EntityDistance(w\obj, n\Collider) 
					;dist2 = dist2 + EntityDistance(w\obj, n\Collider)*2
					If dist2 < dist Then
						If EntityVisible(w\obj, temp) Then
							dist = dist2
							StartPoint = w
						Else
							
						EndIf
					EndIf
				EndIf
			EndIf
		EndIf
	Next
	
	FreeEntity temp
	
	If StartPoint = Null Then DebugLog "startpoint=null" : Return 2
	StartPoint\state = 1		
	
	dist# = 20.0
	For w.WayPoints = Each WayPoints
		xtemp = Abs(EntityX(pvt,True)-EntityX(w\obj,True))
		If xtemp =< 8.0 Then
			ztemp = Abs(EntityZ(pvt,True)-EntityZ(w\obj,True))
			If ztemp =< 8 Then
				dist2# = xtemp+ztemp+Abs(EntityY(w\obj,True)-EntityY(pvt,True)) ;EntityDistance(w\obj, n\Collider) 
				
				;DebugLog dist2
				If dist2 < dist Then	
					dist = dist2
					EndPoint = w
				EndIf				
			EndIf
		EndIf
	Next
	
	FreeEntity pvt
	
	If EndPoint = StartPoint Then 
		DebugLog "startpoint=endpoint" 		
		If dist < 0.4 Then
			Return 0
		Else
			n\Path[0]=EndPoint
			Return 1					
		EndIf
	EndIf
	If EndPoint = Null Then DebugLog "endpoint=null" : Return 2
	
	;aloitus- ja lopetuspisteet löydetty, aletaan etsiä reittiä
	
	;DebugLog "endpoint = startpoint"
	
	Repeat 
		
		temp% = False
		smallest.WayPoints = Null
		dist# = 10000.0
		For w.WayPoints = Each WayPoints
			If w\state = 1 Then
				temp = True
				If (w\Fcost) < dist Then 
					dist = w\Fcost
					smallest = w
				EndIf
			EndIf
		Next
		
		If smallest <> Null Then
			
			w = smallest
			w\state = 2
			;DebugLog ((EntityX(w\obj)+EntityZ(w\obj))+": "+w\Fcost)		
			
			DebugLog "Roomname: "+ w\room\RoomTemplate\Name
			
			For i = 0 To 4
				If w\connected[i]<>Null Then 
					;DebugLog "    - "+(EntityX(w\connected[i]\obj)+EntityZ(w\connected[i]\obj))+", "+w\connected[i]\Fcost+ ", "+w\connected[i]\state
					If w\connected[i]\state < 2 Then 
						
						If w\connected[i]\state=1 Then ;open list
							gtemp# = w\Gcost+w\dist[i]
							If n\NPCtype = NPCtypeMTF Then 
								If w\connected[i]\door = Null Then gtemp = gtemp + 0.5
							EndIf
							If gtemp < w\connected[i]\Gcost Then ;parempi reitti -> overwrite
								;w\connected[i]\tempSteps = w\tempSteps + 1
								w\connected[i]\Gcost = gtemp
								w\connected[i]\Fcost = w\connected[i]\Gcost + w\connected[i]\Hcost
								w\connected[i]\parent = w
							EndIf
						Else
							w\connected[i]\Hcost# = Abs(EntityX(w\connected[i]\obj,True)-EntityX(EndPoint\obj,True))+Abs(EntityZ(w\connected[i]\obj,True)-EntityZ(EndPoint\obj,True))
							gtemp# = w\Gcost+w\dist[i]
							If n\NPCtype = NPCtypeMTF Then 
								If w\connected[i]\door = Null Then gtemp = gtemp + 0.5
							EndIf
							w\connected[i]\Gcost = gtemp
							w\connected[i]\Fcost = w\Gcost+w\Hcost 
							w\connected[i]\parent = w
							w\connected[i]\state=1
							;w\connected[i]\tempSteps = w\tempSteps + 1
						EndIf						
					EndIf
					
				EndIf
			Next
		Else ;open listiltä ei löytynyt mitään
			;DebugLog "open list empty"
			If EndPoint\state > 0 Then 
				StartPoint\parent = Null
				;DebugLog "polku löytynyt" 
				EndPoint\state = 2
				Exit
			EndIf			
			;Return 2
		EndIf
		
		If EndPoint\state > 0 Then 
			StartPoint\parent = Null
			;DebugLog "polku löytynyt" 
			EndPoint\state = 2
			Exit
		EndIf
		
	Until temp = False
	
	If EndPoint\state > 0 Then
		
		currpoint.waypoints = EndPoint
		
		length = 0
		Repeat
			;DebugLog (EntityX(currpoint\obj)+EntityZ(currpoint\obj))
			length = length +1
			currpoint = currpoint\parent
		Until currpoint = Null
		
		DebugLog "length: "+ length		
		
		currpoint.waypoints = EndPoint
		For i = 0 To (length-1)
			;DebugLog i +" -    "+EntityX(currpoint\obj)+EntityZ(currpoint\obj)
			temp =False
			If length < 20 Then
				n\Path[length-1-i] = currpoint.WayPoints
			Else
				If i < 20 Then
					n\Path[20-1-i] = w.WayPoints
				Else
					;Return 1
				EndIf
			EndIf
			
			If currpoint = StartPoint Then Return 1
			
			If currpoint\parent <> Null Then
				currpoint = currpoint\parent
			Else
				Exit
			EndIf
			
		Next
		
	Else
		
		DebugLog "FUNCTION FindPath() - reittiä ei löytynyt"
		Return 2 ;reittiä määränpäähän ei löytynyt
		
	EndIf
	
	
	
End Function


Function CreateLine(x1#,y1#,z1#, x2#,y2#,z2#, mesh=0)
	
	If mesh = 0 Then 
		mesh=CreateMesh()
		EntityFX(mesh,16)
		surf=CreateSurface(mesh)	
		verts = 0	
		
		AddVertex surf,x1#,y1#,z1#,0,0
	Else
		surf = GetSurface(mesh,1)
		verts = CountVertices(surf)-1
	End If
	
	AddVertex surf,(x1#+x2#)/2,(y1#+y2#)/2,(z1#+z2#)/2,0,0 
	; you could skip creating the above vertex and change the line below to
	; AddTriangle surf,verts,verts+1,verts+0
	; so your line mesh would use less vertices, the drawback is that some videocards (like the matrox g400)
	; aren't able to create a triangle with 2 vertices. so, it's your call :)
	AddVertex surf,x2#,y2#,z2#,1,0
	
	AddTriangle surf,verts,verts+2,verts+1
	
	Return mesh
End Function

;-------------------------------------------------------------------------------------------------------

Global SelectedScreen.Screens
Type Screens
	Field obj%
	Field imgpath$
	Field img
	Field room.Rooms
End Type

Type TempScreens
	Field imgpath$
	Field x#,y#,z#
	Field roomtemplate.RoomTemplates
End Type

Function CreateScreen.Screens(x#,y#,z#,imgpath$,r.Rooms)
	s.screens = New Screens
	s\obj = CreatePivot()
	EntityPickMode(s\obj, 1)	
	EntityRadius s\obj, 0.1
	
	PositionEntity s\obj, x,y,z
	s\imgpath = imgpath
	s\room = r
	EntityParent s\obj, r\obj
	
	Return s
End Function

Function UpdateScreens()
	If SelectedScreen <> Null Then Return
	If SelectedDoor <> Null Then Return
	
	For s.screens = Each Screens
		If s\room = PlayerRoom Then
			If EntityDistance(Collider,s\obj)<1.2 Then
				EntityPick(Camera, 1.2)
				If PickedEntity()=s\obj And s\imgpath<>"" Then
					DrawHandIcon=True
					If MouseUp1 Then 
						SelectedScreen=s
						DebugLog s\imgpath
						s\img = LoadImage("GFX\screens\"+s\imgpath)
						MaskImage s\img, 255,0,255
						ResizeImage(s\img, ImageWidth(s\img) * MenuScale, ImageHeight(s\img) * MenuScale)
						
						PlaySound ButtonSFX
						MouseUp1=False
					EndIf
				EndIf
			EndIf
		EndIf
	Next
	
End Function



Dim MapName$(MapWidth, MapHeight)
Dim MapRoomID%(ROOM4 + 1)
Dim MapRoom$(ROOM4 + 1, 0)

Function CreateMap()
	Local x%, y%, temp%
	Local i%, x2%, y2%
	Local width%, height%
	
	;If RandomSeed = "" Then
	;	RandomSeed = Abs(MilliSecs())
	;EndIf
	Local strtemp$ = ""
	For i = 1 To Len(RandomSeed)
		strtemp = strtemp+Asc(Mid(strtemp,i,1))
	Next
	SeedRnd Abs(Int(strtemp))
	
	Dim MapName$(MapWidth, MapHeight)
	
	Local MaxRooms% = 50
	Dim MapRoomID%(ROOM4 + 1)
	Dim MapRoom$(ROOM4 + 1, MaxRooms)
	
	x = Floor(MapWidth / 2)
	y = MapHeight - Rand(3, 5)
	
	For i = y To MapHeight - 1
		MapTemp(lvl, x, i) = True
	Next
	
	Repeat
		width = Rand(8, 15)
		
		If x - width < 2 Then ;käytävän pakko mennä oikealle
			
		ElseIf x + width > (MapWidth - 2) ;käytävän pakko mennä vasemmalle
			width = -width
		Else ;käytävä randomilla johonkin suuntaan
			If x<MapWidth/2 Then
				;x =  Rand(x-(width/2)+1, x)
			Else
				x = x-width+1
			EndIf
			;x = Rand(x-width+1, x)
		EndIf
		
		If x+width > MapWidth-2 Then
			width=width+((MapWidth-2)-(x+width))
		EndIf
		
		x = Min(x, x + width)
		width = Abs(width)
		For i = x To x + width
			DebugLog lvl+", "+i+", "+y
			MapTemp(lvl, Min(i,MapWidth), y) = True
		Next
		
		height = Rand(3, 5)
		If y - height =< 2 Then height = y-1
		
		yhallways = Rand(1,5)
		
		For i = 1 To yhallways
			x2 = Max(Min(Rand(x, x + width),MapWidth-2),2)
			
			;katsotaan ettei tule kahta käytävää vierekkäin
			If MapTemp(lvl, x2 - 1, y - 1) = False And MapTemp(lvl, x2 + 1, y - 1) = False Then
				If i = 1 Then tempheight = height Else tempheight = Max(Rand(height-Rand(0,2)),1)
				DebugLog tempheight+" - "+height
				For y2 = y - tempheight To y
					MapTemp(lvl, x2, y2) = True
					
					If Rand(2) = 1 Then ;laitetaan randomilla yksittäisiä huoneita pystykäytävien viereen
						If x2 > 2 And x2 < MapWidth-2 And y2 > 2 And y2 < MapHeight-2 And y2<y Then 
							If Rand(2) = 1 Then dir = -1 Else dir = 1
							Local empty = True
							For xtemp= Min(x2+dir*2, x2+dir) To Max(x2+dir*2, x2+dir)
								For ztemp = y2-1 To y2+1
									If MapTemp(lvl, xtemp, ztemp) = True Then empty = False : Exit
								Next
								If empty = False Then Exit
							Next
							If empty Then MapTemp(lvl, x2+dir, y2) = True 
						EndIf
					EndIf
				Next
				If tempheight = height Then temp = x2
			End If
			
		Next
		
		x = temp
		y = y - height
	Until y <= 2
	
	
	Local ZoneAmount=3
	Local Room1Amount%[3], Room2Amount%[3],Room2CAmount%[3],Room3Amount%[3],Room4Amount%[3]
	
	
	For y = 1 To MapHeight - 1
		If y < MapHeight/3+1 Then
			zone=2
		ElseIf y < MapHeight*(2.0/3.0)-1
			zone=1
		Else
			zone=0
		EndIf
		
		For x = 1 To MapWidth - 1
			If MapTemp(lvl, x, y) > 0 Then
				temp = Min(MapTemp(lvl, x + 1, y),1) + Min(MapTemp(lvl, x - 1, y),1)
				temp = temp + Min(MapTemp(lvl, x, y + 1),1) + Min(MapTemp(lvl, x, y - 1),1)					
				MapTemp(lvl, x, y) = temp
				Select MapTemp(lvl,x,y)
					Case 1
						Room1Amount[zone]=Room1Amount[zone]+1
					Case 2
						If Min(MapTemp(lvl, x + 1, y),1) + Min(MapTemp(lvl, x - 1, y),1)= 2 Then
							Room2Amount[zone]=Room2Amount[zone]+1	
						ElseIf Min(MapTemp(lvl, x, y + 1),1) + Min(MapTemp(lvl, x , y - 1),1)= 2
							Room2Amount[zone]=Room2Amount[zone]+1	
						Else
							Room2CAmount[zone] = Room2CAmount[zone]+1
						EndIf
					Case 3
						Room3Amount[zone]=Room3Amount[zone]+1	
					Case 4
						Room4Amount[zone]=Room4Amount[zone]+1
				End Select
				
			EndIf
		Next
	Next		
	
	;ROOM1-huoneita liian vähän -> tehdään randomilla lisää
	For i = 0 To -1;ZoneAmount-1
		temp = -Room1Amount[zone]+9
		
		If temp > 0 Then
			For y = 2 To MapHeight/(3*(zone+1))
				If y < MapHeight/3+1 Then
					zone=2
				ElseIf y < MapHeight*(2.0/3.0)-1
					zone=1
				Else
					zone=0
				EndIf				
				
				For x = 2 To MapWidth - 2
					If MapTemp(lvl, x, y) = 0 Then
						If Rand(7)=1 
							If (MapTemp(lvl, x + 1, y) + MapTemp(lvl, x - 1, y) + MapTemp(lvl, x, y + 1) + MapTemp(lvl, x, y - 1)) > 1 Then
								If (Min(MapTemp(lvl, x + 1, y),1) + Min(MapTemp(lvl, x - 1, y),1) + Min(MapTemp(lvl, x, y + 1),1) + Min(MapTemp(lvl, x, y - 1),1)) = 1 Then
								
									MapTemp(lvl, x, y) = 1
									Room1Amount[zone] = Room1Amount[zone]+1
									temp=temp-1
								EndIf
							EndIf
						EndIf
					EndIf
					If temp = 0 Then Exit
				Next
				If temp = 0 Then Exit
			Next
		EndIf
	Next
	
	;zone 1
	
	MapRoom(ROOM2, 0) = "room2closets"
	MapRoom(ROOM2C, 0) = "lockroom"	
	MapRoom(ROOM1, Floor(0.4*Float(Room1Amount[0]))) = "roompj"	
	MapRoom(ROOM1, Floor(0.8*Float(Room1Amount[0]))) = "914"	
	MapRoom(ROOM1, 0) = "start"
	
	MapRoom(ROOM2, Floor(0.4*Float(Room2Amount[0]))) = "room2testroom2"	
	MapRoom(ROOM2, Floor(0.6*Float(Room2Amount[0]))) = "room012"
	
	;zone 2
	MapRoom(ROOM1, Room1Amount[0]+Floor(0.1*Float(Room1Amount[1]))) = "008"	
	MapRoom(ROOM1, Room1Amount[0]+Floor(0.4*Float(Room1Amount[1]))) = "coffin"		
	MapRoom(ROOM1, Room1Amount[0]+Floor(0.6*Float(Room1Amount[1]))) = "room079"
	MapRoom(ROOM1, Room1Amount[0]+Floor(0.8*Float(Room1Amount[1]))) = "room106"	
	
	MapRoom(ROOM2, Room2Amount[0]+Floor(0.2*Float(Room2Amount[1]))) = "room2nuke"
	MapRoom(ROOM2, Room2Amount[0]+Floor(0.4*Float(Room2Amount[1]))) = "room2tunnel"	
	MapRoom(ROOM2, Room2Amount[0]+Floor(0.6*Float(Room2Amount[1]))) = "room049"	
	MapRoom(ROOM2, Room2Amount[0]+Floor(0.8*Room2Amount[1])) = "room2servers"
	MapRoom(ROOM2, Room2Amount[0]+Floor(0.95*Float(Room2Amount[1]))) = "testroom"	
	
	;zone 3
	MapRoom(ROOM1, Room1Amount[0]+Room1Amount[1]+Room1Amount[2]-2) = "exit1"
	MapRoom(ROOM1, Room1Amount[0]+Room1Amount[1]+Room1Amount[2]-1) = "gateaentrance"	
	
	MapRoom(ROOM2, Room2Amount[0]+Room2Amount[1]+Floor(Rnd(0.1,0.15)*Float(Room2Amount[2]))) = "room2offices"	
	MapRoom(ROOM2, Room2Amount[0]+Room2Amount[1]+Floor(0.3*Float(Room2Amount[2]))) = "room2offices2"
	MapRoom(ROOM2, Floor(0.5*Float(Room2Amount[0]))) = "room2sroom"
	MapRoom(ROOM2, Floor(0.7*Room2Amount[0])) = "room2scps"
	MapRoom(ROOM2, Room2Amount[0]+Room2Amount[1]+Floor(Rnd(0.8,0.85)*Float(Room2Amount[2]))) = "room2poffices"
	
	MapRoom(ROOM2C, Room2CAmount[0]+Room2CAmount[1]) = "room2ccont"	
	MapRoom(ROOM2C, Room2CAmount[0]+Room2CAmount[1]+1) = "lockroom2"		
	
	MapRoom(ROOM3, Room3Amount[0]+Room3Amount[1]+Floor(0.3*Float(Room3Amount[2]))) = "room3servers"
	MapRoom(ROOM3, Room3Amount[0]+Room3Amount[1]+Floor(0.7*Float(Room3Amount[2]))) = "room3servers2"
	
	For y = 0 To MapHeight
		For x = 0 To MapWidth
			MapTemp(lvl, x, y) = Min(MapTemp(lvl, x, y),1)
		Next
	Next
	
	;----------------------- luodaan kartta --------------------------------
	
	temp = 0
	Local r.Rooms, spacing# = 8.0
	For y = MapHeight - 1 To 1 Step - 1
		
		If y < MapHeight/3+1 Then
			zone=3
		ElseIf y < MapHeight*(2.0/3.0)-1
			zone=2
		Else
			zone=1
		EndIf
		
		For x = 1 To MapWidth - 2
			If MapTemp(lvl, x, y) > 0 Then
				
				temp = MapTemp(lvl, x + 1, y) + MapTemp(lvl, x - 1, y) + MapTemp(lvl, x, y + 1) + MapTemp(lvl, x, y - 1)
				
				Select temp ;viereisissä ruuduissa olevien huoneiden määrä
					Case 1
						If MapRoomID(ROOM1) < MaxRooms And MapName(x,y) = "" Then
							If MapRoom(ROOM1, MapRoomID(ROOM1)) <> "" Then MapName(x, y) = MapRoom(ROOM1, MapRoomID(ROOM1)) ;: DebugLog (x + ", " + y + " - " + MapRoom[ROOM1, MapRoomID[ROOM1]])
						EndIf
						r = CreateRoom(zone, ROOM1, x * 8, 0, y * 8, MapName(x, y))
						If MapTemp(lvl, x, y + 1) Then
							r\angle = 180 
							TurnEntity(r\obj, 0, r\angle, 0)
						ElseIf MapTemp(lvl, x - 1, y)
							r\angle = 270
							TurnEntity(r\obj, 0, r\angle, 0)
						ElseIf MapTemp(lvl, x + 1, y)
							r\angle = 90
							TurnEntity(r\obj, 0, r\angle, 0)
						Else 
							r\angle = 0
						End If
						
						MapRoomID(ROOM1)=MapRoomID(ROOM1)+1
					Case 2
						If MapTemp(lvl, x - 1, y)And MapTemp(lvl, x + 1, y) Then
							If MapRoomID(ROOM2) < MaxRooms And MapName(x,y) = ""  Then
								If MapRoom(ROOM2, MapRoomID(ROOM2)) <> "" Then MapName(x, y) = MapRoom(ROOM2, MapRoomID(ROOM2))
							EndIf
							r = CreateRoom(zone, ROOM2, x * 8, 0, y * 8, MapName(x, y))
							If Rand(2) = 1 Then r\angle = 90 Else r\angle = 270
							TurnEntity(r\obj, 0, r\angle, 0)
							MapRoomID(ROOM2)=MapRoomID(ROOM2)+1
						ElseIf MapTemp(lvl, x, y - 1) And MapTemp(lvl, x, y + 1)
							If MapRoomID(ROOM2) < MaxRooms And MapName(x,y) = ""  Then
								If MapRoom(ROOM2, MapRoomID(ROOM2)) <> "" Then MapName(x, y) = MapRoom(ROOM2, MapRoomID(ROOM2))
							EndIf
							r = CreateRoom(zone, ROOM2, x * 8, 0, y * 8, MapName(x, y))
							If Rand(2) = 1 Then r\angle = 180 Else r\angle = 0
							TurnEntity(r\obj, 0, r\angle, 0)								
							MapRoomID(ROOM2)=MapRoomID(ROOM2)+1	
						ElseIf MapTemp(lvl, x - 1, y) And MapTemp(lvl, x, y + 1)
							If MapRoomID(ROOM2C) < MaxRooms And MapName(x,y) = ""  Then
								If MapRoom(ROOM2C, MapRoomID(ROOM2C)) <> "" Then MapName(x, y) = MapRoom(ROOM2C, MapRoomID(ROOM2C))
							EndIf
							r = CreateRoom(zone, ROOM2C, x * 8, 0, y * 8, MapName(x, y))
							r\angle = 180
							TurnEntity(r\obj, 0, r\angle, 0)
							MapRoomID(ROOM2C)=MapRoomID(ROOM2C)+1
						ElseIf MapTemp(lvl, x + 1, y) And MapTemp(lvl, x, y + 1)
							If MapRoomID(ROOM2C)< MaxRooms And MapName(x,y) = ""  Then
								If MapRoom(ROOM2C, MapRoomID(ROOM2C)) <> "" Then MapName(x, y) = MapRoom(ROOM2C, MapRoomID(ROOM2C))
							EndIf
							r = CreateRoom(zone, ROOM2C, x * 8, 0, y * 8, MapName(x, y))
							r\angle = 90
							TurnEntity(r\obj, 0, r\angle, 0)
							MapRoomID(ROOM2C)=MapRoomID(ROOM2C)+1		
						ElseIf MapTemp(lvl, x - 1, y) And MapTemp(lvl, x, y - 1)
							If MapRoomID(ROOM2C) < MaxRooms And MapName(x,y) = "" Then
								If MapRoom(ROOM2C, MapRoomID(ROOM2C)) <> "" Then MapName(x, y) = MapRoom(ROOM2C, MapRoomID(ROOM2C))
							EndIf
							r = CreateRoom(zone, ROOM2C, x * 8, 0, y * 8, MapName(x, y))
							TurnEntity(r\obj, 0, 270, 0)
							r\angle = 270
							MapRoomID(ROOM2C)=MapRoomID(ROOM2C)+1		
						Else
							If MapRoomID(ROOM2C)< MaxRooms And MapName(x,y) = "" Then
								If MapRoom(ROOM2C, MapRoomID(ROOM2C)) <> "" Then MapName(x, y) = MapRoom(ROOM2C, MapRoomID(ROOM2C))
							EndIf
							r = CreateRoom(zone, ROOM2C, x * 8, 0, y * 8, MapName(x, y))
							MapRoomID(ROOM2C)=MapRoomID(ROOM2C)+1
						EndIf
					Case 3
						If MapRoomID(ROOM3) < MaxRooms And MapName(x,y) = "" Then
							If MapRoom(ROOM3, MapRoomID(ROOM3)) <> "" Then MapName(x, y) = MapRoom(ROOM3, MapRoomID(ROOM3))
						EndIf
						r = CreateRoom(zone, ROOM3, x * 8, 0, y * 8, MapName(x, y))
						If (Not MapTemp(lvl, x, y - 1)) Then
							TurnEntity(r\obj, 0, 180, 0)
							r\angle = 180
						ElseIf (Not MapTemp(lvl, x - 1, y))
							TurnEntity(r\obj, 0, 90, 0)
							r\angle = 90
						ElseIf (Not MapTemp(lvl, x + 1, y))
							TurnEntity(r\obj, 0, -90, 0)
							r\angle = 270
						End If
						MapRoomID(ROOM3)=MapRoomID(ROOM3)+1
					Case 4
						If MapRoomID(ROOM4) < MaxRooms And MapName(x,y) = "" Then
							If MapRoom(ROOM4, MapRoomID(ROOM4)) <> "" Then MapName(x, y) = MapRoom(ROOM4, MapRoomID(ROOM4))
						EndIf
						r = CreateRoom(zone, ROOM4, x * 8, 0, y * 8, MapName(x, y))
						MapRoomID(ROOM4)=MapRoomID(ROOM4)+1
				End Select
				
				If (Floor((x + y) / 2.0) = Ceil((x + y) / 2.0)) Then
					If zone = 2 Then temp = 2 Else temp=0
					
					If MapTemp(lvl, x + 1, y) Then
						CreateDoor(r\level, x * spacing + spacing / 2.0, 0, y * spacing, 90, Null, Max(Rand(-3, 1), 0), temp)
					EndIf
					
					If MapTemp(lvl, x - 1, y) Then
						CreateDoor(r\level, x * spacing - spacing / 2.0, 0, y * spacing, 90, Null, Max(Rand(-3, 1), 0), temp)
					EndIf
					
					If MapTemp(lvl, x, y + 1) Then
						CreateDoor(r\level, x * spacing, 0, y * spacing + spacing / 2.0, 0, Null, Max(Rand(-3, 1), 0), temp)
					EndIf
					
					If MapTemp(lvl, x, y - 1) Then
						CreateDoor(r\level, x * spacing, 0, y * spacing - spacing / 2.0, 0, Null, Max(Rand(-3, 1), 0), temp)
					EndIf
				End If					
				
			End If
			
		Next
	Next		
	
	r = CreateRoom(0, ROOM1, 8, 0, (MapHeight-1) * 8, "173")
	MapRoomID(ROOM1)=MapRoomID(ROOM1)+1
	
	r = CreateRoom(0, ROOM1, (MapWidth-1) * 8, 0, (MapHeight-1) * 8, "pocketdimension")
	MapRoomID(ROOM1)=MapRoomID(ROOM1)+1	
	
	r = CreateRoom(0, ROOM1, 0, 0, 8, "gatea")
	MapRoomID(ROOM1)=MapRoomID(ROOM1)+1		
	
	If 0 Then 
		Repeat
			;RenderWorld
			Cls
			For x = 0 To MapWidth - 1
				For y = 0 To MapHeight - 1
					If MapTemp(0, x, y) = 0 Then
						
						If y < MapHeight/3+1 Then
							zone=3
						ElseIf y < MapHeight*(2.0/3.0)-1
							zone=2
						Else
							zone=1
						EndIf
						
						Color 50*zone, 50*zone, 50*zone
						Rect(x * 32, y * 32, 30, 30)
					Else
						Color 255, 255, 255
						Rect(x * 32, y * 32, 30, 30)
					End If
				Next
			Next	
			
			Color 255, 0, 0
			
			For x = 0 To MapWidth - 1
				For y = 0 To MapHeight - 1
					If MapTemp(0,x, y) > 0 Then
						Text x * 32 +2, y * 32 + 2, MapName(x,y)
					End If
				Next
			Next			
			
			Flip
		Until KeyHit(28)		
	EndIf
End Function








;~IDEal Editor Parameters:
;~F#2#9#26#FF#110#121#13C#150#15B#166#1A2#1B1#1F2#898#8C3#8DE#8E3#8F2#909#995
;~F#A6E#A8B#A92#A98#AA6
;~C#Blitz3D