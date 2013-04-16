
Include "FastExt.bb"
Global OptionFile$ = "options.ini"

Global Font1

Global VersionNumber$ = "0.7.1"

AppTitle = "SCP - Containment Breach Launcher"

Global MenuWhite%, MenuBlack%
Global ButtonSFX%

Dim ArrowIMG(4)

;[Block]

Global LauncherWidth%= Min(GetINIInt(OptionFile, "launcher", "launcher width"), 1024)
Global LauncherHeight% = Min(GetINIInt(OptionFile, "launcher", "launcher height"), 768)
Global LauncherEnabled% = GetINIInt(OptionFile, "launcher", "launcher enabled")
Global LauncherIMG%

Global GraphicWidth% = GetINIInt(OptionFile, "options", "width")
Global GraphicHeight% = GetINIInt(OptionFile, "options", "height")
Global Depth% = 0, Fullscreen% = GetINIInt(OptionFile, "options", "fullscreen")

Global ScrollGFXDriver%, ScrollAudioDriver%

Global SelectedGFXMode%
Global SelectedGFXDriver% = Max(GetINIInt(OptionFile, "options", "gfx driver"), 1)
Global SelectedAudioDriver% = GetINIInt(OptionFile, "options", "audio driver")
;Global AudioDriver$[] = AudioDrivers()

Global ShowFPS = GetINIInt(OptionFile, "options", "show FPS")

Global TotalGFXModes% = CountGfxModes3D(), GFXModes%
Dim GfxModeWidths%(TotalGFXModes), GfxModeHeights%(TotalGFXModes)

If LauncherEnabled Then 
	UpdateLauncher()
	
	If Fullscreen Then
		Graphics3D(GraphicWidth, GraphicHeight, Depth, 1)
	Else
		Graphics3D(GraphicWidth, GraphicHeight, Depth, 2)
	End If
	
Else
	For i% = 1 To TotalGFXModes
		Local samefound% = False
		For  n% = 0 To TotalGFXModes - 1
			If GfxModeWidths(n) = GfxModeWidth(i) And GfxModeHeights(n) = GfxModeHeight(i) Then samefound = True : Exit
		Next
		If samefound = False Then
			If GraphicWidth = GfxModeWidth(i) And GraphicHeight = GfxModeHeight(i) Then SelectedGFXMode = GFXModes
			GfxModeWidths(GFXModes) = GfxModeWidth(i)
			GfxModeHeights(GFXModes) = GfxModeHeight(i)
			GFXModes=GFXModes+1 
		End If
	Next
	
	GraphicWidth =GfxModeWidths(SelectedGFXMode)
	GraphicHeight = GfxModeHeights(SelectedGFXMode)
	
	If Fullscreen Then
		Graphics3D(GraphicWidth, GraphicHeight, Depth, 1)
	Else
		Graphics3D(GraphicWidth, GraphicHeight, Depth, 2)
	End If
	
	initext
	
EndIf

AppTitle = "SCP - Containment Breach v"+VersionNumber

;If SetAudioDriver(AudioDriver[SelectedAudioDriver]) = False Then RuntimeError("Unable to set audio driver (" + AudioDriver[SelectedAudioDriver] + ")")
;bbSetGfxDriver(SelectedGFXDriver)


;AntiAlias True
SetBuffer BackBuffer()

Global CurTime%, PrevTime%, LoopDelay%, FPSfactor#, FPSfactor2#
Local CheckFPS%, ElapsedLoops%, FPS%, Framelimit% = 60, ElapsedTime#

Const HIT_MAP% = 1, HIT_PLAYER% = 2, HIT_ITEM% = 3, HIT_APACHE% = 4
SeedRnd MilliSecs()

;[End block]

Global GameSaved%

;---------------------------------------------------------------------------------------------------------------------

;[Block]

Global CursorIMG = LoadImage("GFX\cursor.png")

Global SelectedLoadingScreen.LoadingScreens, LoadingScreenAmount%, LoadingScreenText%
;Global CwmImg% = LoadImage("GFX\cwm.jpg")
Global LoadingBack = LoadImage("Loadingscreens\loadingback.jpg")
InitLoadingScreens("Loadingscreens\loadingscreens.ini")

Font1 = LoadFont("GFX\TI-83.ttf", Int(18 * (GraphicHeight / 1024.0)))
Global Font2% = LoadFont("GFX\TI-83.ttf", Int(60 * (GraphicHeight / 1024.0)))
SetFont Font2

Global BlinkMeterIMG% = LoadImage("GFX\blinkmeter.jpg")

DrawLoading(0, True)

; - -Viewport.
Global viewport_center_x% = GraphicWidth / 2, viewport_center_y% = GraphicHeight / 2

; -- Mouselook.
Global mouselook_x_inc# = 0.3 ; This sets both the sensitivity And direction (+/-) of the mouse on the X axis.
Global mouselook_y_inc# = 0.3 ; This sets both the sensitivity And direction (+/-) of the mouse on the Y axis.
Global mouse_left_limit% = 250 ; Used To limit the mouse movement To within a certain number of pixels (250 is used here) from the center of the screen. This produces smoother mouse movement than continuously moving the mouse back To the center Each loop.
Global mouse_right_limit% = GraphicsWidth () - 250 ; As above.
Global mouse_top_limit% = 250 ; As above.
Global mouse_bottom_limit% = GraphicsHeight () - 250 ; As above.

Global KEY_RIGHT=GetINIInt(OptionFile, "options", "Right key"), KEY_LEFT=GetINIInt(OptionFile, "options", "Left key")

Global KEY_UP=GetINIInt(OptionFile, "options", "Up key"), KEY_DOWN=GetINIInt(OptionFile, "options", "Down key")
Global KEY_BLINK=GetINIInt(OptionFile, "options", "Blink key"), KEY_SPRINT=GetINIInt(OptionFile, "options", "Sprint key")
Global KEY_INV=GetINIInt(OptionFile, "options", "Inventory key"), KEY_CROUCH=GetINIInt(OptionFile, "options", "Crouch key")

; -- Mouse smoothing que.
Global mouse_x_speed_1#, mouse_y_speed_1#

Const Infinity# = (999.0) ^ (99999.0), Nan# = (-1.0) ^ (0.5)

Global Mesh_MinX#, Mesh_MinY#, Mesh_MinZ#
Global Mesh_MaxX#, Mesh_MaxY#, Mesh_MaxZ#
Global Mesh_MagX#, Mesh_MagY#, Mesh_MagZ#

Global Shake#, KillTimer#, FallTimer#, KillAnim%, DropSpeed#, HeadDropSpeed#, CurrSpeed#
Global Sanity#, MoveLock%, ForceMove#

Global SCP1025state%[6]

Global HeartBeatRate#, HeartBeatTimer#, HeartBeatVolume#

; -- User.
Global user_camera_pitch#
Global up#, side#, PlayerRoom.Rooms
Global MainMenuOpen%, MenuOpen%, InvOpen%

Global Crouch%, CrouchState#

Global PlayerLevel%
Global SelectedEnding$, EndingScreen%, EndingTimer#

Global GrabbedEntity%

Const BLINKFREQ% = 70 * 8
Global BlinkTimer#, Stamina#

Global InvertMouse% = GetINIInt(OptionFile, "options", "invert mouse y")

Global MouseHit1%, MouseDown1%, MouseHit2%, DoubleClick%, LastMouseHit1%, MouseUp1%

Global MsgTimer#, Msg$
Global WearingGasMask%, WearingHazmat%, EyeIrritation#, EyeSuper#, EyeStuck#
Global WearingVest%, Injuries#, Bloodloss#, Infect#
Global Wearing714%

Global ExplosionTimer#, ExplosionSFX%

Global AccessCode%, KeypadInput$, KeypadTimer#, KeypadMSG$

Global MTFtimer#, MTFrooms.Rooms[10], MTFroomState%[10]

Global GodMode%, NoClip%, NoClipSpeed# = 2.0

Global DrawHandIcon%
Dim DrawArrowIcon%(4)

Global SuperMan%, SuperManTimer#
Global LightsOn% = True

Const MAXACHIEVEMENTS=21
Dim Achievements%(MAXACHIEVEMENTS+1)
Const Achv420%=0, Achv106%=1, Achv372%=2, Achv895%=3, Achv079%=4, Achv914%=5, Achv789%=6, Achv096%=7
Const Achv012%=8, Achv049%=9, Achv1025%=10, Achv714%=12, Achv008%=13, Achv500%=14
Const AchvTesla%=15, AchvMaynard%=16, AchvHarp%=17, AchvPD%=18, AchvSNAV%=19, AchvOmni%=20, AchvConsole%=21
Global RefinedItems%

Global PlayerSoundVolume#

Global SoundTransmission%

Global CoffinDistance#

Dim RadioState#(10)
Dim RadioCHN%(8)

Dim OldAiPics%(10)

Global PlayTime%

;[End block]


;----------------------------------------------  Console -----------------------------------------------------

Global ConsoleOpen%, ConsoleInput$

Type ConsoleMsg
	Field txt$
End Type

Function CreateConsoleMsg(txt$)
	Local c.ConsoleMsg = New ConsoleMsg
	Insert c Before First ConsoleMsg
	
	c\txt = txt
End Function

Function UpdateConsole()
	
	If ConsoleOpen Then
		Local x% = 20, y% = 20, width% = 400, height% = 500
		Local StrTemp$, temp%,  i%
		Local ev.Events, r.Rooms, it.Items
		
		Color 255, 255, 255
		Rect(x, y, width, height)
		Color 0,0,0
		Rect(x + 10, y + 10, width - 20, height - 60)
		
		Rect(x + 10, y + height - 40, width - 20, 30)
		
		Color 255, 255, 255
		
		Text (x + 30, y + height - 35, ConsoleInput)
		
		SelectedInputBox = 2
		ConsoleInput = InputBox(x + 10, y + height - 40, width - 20, 30, ConsoleInput, 2)
		ConsoleInput = Left(ConsoleInput, 50)
		
		Local z%, x2%, y2%, z2%
		If KeyHit(28) And ConsoleInput <> "" Then
			Achievements(AchvConsole) = False
			
			If Instr(ConsoleInput, " ") > 0 Then
				StrTemp$ = Lower(Left(ConsoleInput, Instr(ConsoleInput, " ") - 1))
			Else
				StrTemp$ = Lower(ConsoleInput)
			End If
			
			Select Lower(StrTemp)
				Case "asd"
					WireFrame 1
					GodMode = 1
					NoClip = 1
					CameraFogNear = 15
					CameraFogFar = 20
				Case "status"
					CreateConsoleMsg("******************************")
					CreateConsoleMsg("Status: ")
					CreateConsoleMsg("Coordinates: ")
					CreateConsoleMsg("    - collider: "+EntityX(Collider)+", "+EntityY(Collider)+", "+EntityZ(Collider))
					CreateConsoleMsg("    - camera: "+EntityX(Camera)+", "+EntityY(Camera)+", "+EntityZ(Camera))
					
					CreateConsoleMsg("Rotation: ")
					CreateConsoleMsg("    - collider: "+EntityPitch(Collider)+", "+EntityYaw(Collider)+", "+EntityRoll(Collider))
					CreateConsoleMsg("    - camera: "+EntityPitch(Camera)+", "+EntityYaw(Camera)+", "+EntityRoll(Camera))
					
					CreateConsoleMsg("Room: "+PlayerRoom\RoomTemplate\Name)
					For ev.Events = Each Events
						If ev\room = PlayerRoom Then
							CreateConsoleMsg("Room event: "+ev\EventName)	
							CreateConsoleMsg("-    state: "+ev\EventState)
							CreateConsoleMsg("-    state2: "+ev\EventState2)	
							CreateConsoleMsg("-    state3: "+ev\EventState3)
							Exit
						EndIf
					Next
					
					CreateConsoleMsg("Room coordinates: "+Floor(EntityX(PlayerRoom\obj) / 8.0 + 0.5)+", "+ Floor(EntityZ(PlayerRoom\obj) / 8.0 + 0.5))
					CreateConsoleMsg("Stamina: "+Stamina)
					CreateConsoleMsg("Death timer: "+KillTimer)					
					CreateConsoleMsg("Blinktimer: "+BlinkTimer)
					CreateConsoleMsg("Injuries: "+Injuries)
					CreateConsoleMsg("Bloodloss: "+Bloodloss)
					CreateConsoleMsg("******************************")
				Case "camerapick"
					e = CameraPick(Camera,GraphicWidth/2, GraphicHeight/2)
					If e = 0 Then
						CreateConsoleMsg("******************************")
						CreateConsoleMsg("No entity  picked")
						CreateConsoleMsg("******************************")								
					Else
						CreateConsoleMsg("******************************")
						CreateConsoleMsg("Picked entity:")
						sf = GetSurface(e,1)
						b = GetSurfaceBrush( sf )
						t = GetBrushTexture(b,0)
						texname$ =  StripPath(TextureName(t))
						CreateConsoleMsg("Texture name: "+texname)
						CreateConsoleMsg("Coordinates:"+EntityX(e)+", "+EntityY(e)+", "+EntityZ(e))
						CreateConsoleMsg("******************************")							
					EndIf
				Case "ending"
					SelectedEnding = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					KillTimer = -0.1
					EndingTimer = -0.1
				Case "noclipspeed"
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					NoClipSpeed = Float(StrTemp)
				Case "injure"
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Injuries = Float(StrTemp)
				Case "infect"					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Infect = Float(StrTemp)
				Case "heal"
					Injuries = 0
					Bloodloss = 0
				Case "teleport"
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Select StrTemp
						Case "895", "scp-895"
							StrTemp = "coffin"
						Case "scp-914"
							StrTemp = "914"
						Case "offices", "office"
							StrTemp = "room2offices"
					End Select
					
					For r.Rooms = Each Rooms
						If r\RoomTemplate\Name = StrTemp Then
							PositionEntity (Collider, EntityX(r\obj), 0.7, EntityZ(r\obj))
							ResetEntity(Collider)
							UpdateDoors()
							UpdateRooms()
							PlayerRoom = r
							Exit
						EndIf
					Next
				Case "spawnitem"
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					temp = False 
					For itt.Itemtemplates = Each ItemTemplates
						If Lower(itt\name) = StrTemp Then
							temp = True
							CreateConsoleMsg(StrTemp + " spawned")
							it.Items = CreateItem(itt\name, itt\tempname, EntityX(Collider), EntityY(Camera,True), EntityZ(Collider))
							EntityType(it\obj, HIT_ITEM)
							Exit
						End If
					Next
					
					If temp = False Then CreateConsoleMsg("Item not found")
				Case "wireframe"
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Select StrTemp
						Case "on", "1", "true"
							WireFrame True 
							CreateConsoleMsg("WIREFRAME ON")							
						Case "off", "0", "false"
							WireFrame False
							CreateConsoleMsg("WIREFRAME OFF")								
					End Select
				Case "173speed"
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					Curr173\Speed = Float(StrTemp)
					CreateConsoleMsg("173's speed set to " + StrTemp)
				Case "106speed"
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					Curr106\Speed = Float(StrTemp)
					CreateConsoleMsg("106's speed set to " + StrTemp)
				Case "173state"
					CreateConsoleMsg("SCP-173")
					CreateConsoleMsg("Position: " + EntityX(Curr173\obj) + ", " + EntityY(Curr173\obj) + ", " + EntityZ(Curr173\obj))
					CreateConsoleMsg("Idle: " + Curr173\Idle)
					CreateConsoleMsg("State: " + Curr173\State)
				Case "106state"
					CreateConsoleMsg("SCP-106")
					CreateConsoleMsg("Position: " + EntityX(Curr106\obj) + ", " + EntityY(Curr106\obj) + ", " + EntityZ(Curr106\obj))
					CreateConsoleMsg("Idle: " + Curr106\Idle)
					CreateConsoleMsg("State: " + Curr106\State)
				Case "spawn106"
					Curr106\State = -10
					PositionEntity Curr106\Collider, EntityX(Collider), EntityY(Curr106\Collider), EntityZ(Collider)
				Case "reset096"
               For n.NPCs = Each NPCs
                  If n\NPCtype = NPCtype096 Then
                     RemoveNPC(n)
                     CreateEvent("lockroom096", "lockroom2", 0)   
                     Exit
                  EndIf
               Next
				Case "disable173"
					Curr173\Idle = True
					Disabled173=True
				Case "enable173"
					Curr173\Idle = False
					Disabled173=False
					ShowEntity Curr173\obj
					ShowEntity Curr173\Collider
				Case "disable106"
					Curr106\Idle = True
					Curr106\State = 200000
					Contained106 = True
				Case "enable106"
					Curr106\Idle = False
				Case "halloween"
					Local tex = LoadTexture("GFX\npcs\173h.pt")
					EntityTexture Curr173\obj, tex, 0, 2
				Case "sanic"
					SuperMan = True
					CreateConsoleMsg("GOTTA GO FAST")
				Case "scp-420-j","420","weed"
					For i = 1 To 20
						If Rand(2)=1 Then
							it.Items = CreateItem("Some SCP-420-J","420", EntityX(Collider,True)+Cos((360.0/20.0)*i)*Rnd(0.3,0.5), EntityY(Camera,True), EntityZ(Collider,True)+Sin((360.0/20.0)*i)*Rnd(0.3,0.5))
						Else
							it.Items = CreateItem("Joint","420s", EntityX(Collider,True)+Cos((360.0/20.0)*i)*Rnd(0.3,0.5), EntityY(Camera,True), EntityZ(Collider,True)+Sin((360.0/20.0)*i)*Rnd(0.3,0.5))
						EndIf
						EntityType (it\obj, HIT_ITEM)
					Next
					TempSound = LoadSound("SFX\Mandeville.ogg")
					PlaySound TempSound
				Case "godmode"
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Select StrTemp
						Case "on", "1", "true"
							GodMode = True
							CreateConsoleMsg("GODMODE ON")							
						Case "off", "0", "false"
							GodMode = False
							CreateConsoleMsg("GODMODE OFF")								
					End Select	
				Case "noclip"
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Select StrTemp
						Case "on", "1", "true"
							NoClip = True
							CreateConsoleMsg("NOCLIP ON")							
						Case "off", "0", "false"
							NoClip = False
							CreateConsoleMsg("NOCLIP OFF")		
							RotateEntity Collider, 0, EntityYaw(Collider), 0
					End Select
					
					DropSpeed = 0
				;In console select
				Case "096state"
					For n.NPCs = Each NPCs
						If n\NPCtype = NPCtype096 Then
							CreateConsoleMsg("SCP-096")
							CreateConsoleMsg("Position: " + EntityX(n\obj) + ", " + EntityY(n\obj) + ", " + EntityZ(n\obj))
							CreateConsoleMsg("Idle: " + n\Idle)
							CreateConsoleMsg("State: " + n\State)
							Exit
						EndIf
					Next
					CreateConsoleMsg("SCP-096 has not spawned")
					
				Case "debughud"
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					Select StrTemp
						Case "on", "1", "true"
							DebugHUD = True
							CreateConsoleMsg("Debug Mode On")
						Case "off", "0", "false"
							DebugHUD = False
							CreateConsoleMsg("Debug Mode Off")
					End Select
					
				Case "camerafog"
					args$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					CameraFogNear = Float(Left(args, Len(args) - Instr(args, " ")))
					CameraFogFar = Float(Right(args, Len(args) - Instr(args, " ")))
					CreateConsoleMsg("Near set to: " + CameraFogNear + ", far set to: " + CameraFogFar)
					
				Case "brightness"
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					Brightness = Int(StrTemp)
					CreateConsoleMsg("Brightness set to " + Brightness)
				Case "spawn"
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					Select StrTemp 
						Case "mtf"
							n.NPCs = CreateNPC(NPCtypeMTF, EntityX(Collider),EntityY(Collider)+0.2,EntityZ(Collider))
						Case "173","scp173","scp-173"
							n.NPCs = CreateNPC(NPCtype173, EntityX(Collider),EntityY(Collider)+0.2,EntityZ(Collider))
						Case "106","scp106","scp-106","larry"
							n.NPCs = CreateNPC(NPCtypeOldMan, EntityX(Collider),EntityY(Collider)+0.2,EntityZ(Collider))	
						Case "guard"
							n.NPCs = CreateNPC(NPCtypeGuard, EntityX(Collider),EntityY(Collider)+0.2,EntityZ(Collider))		
						Case "096","scp096","scp-096"
							n.NPCs = CreateNPC(NPCtype096, EntityX(Collider),EntityY(Collider)+0.2,EntityZ(Collider))			
						Case "049","scp049","scp-049"
							n.NPCs = CreateNPC(NPCtype049, EntityX(Collider),EntityY(Collider)+0.2,EntityZ(Collider))		
							n\state = 2
						Case "zombie","scp-049-2"
							n.NPCs = CreateNPC(NPCtypeZombie, EntityX(Collider),EntityY(Collider)+0.2,EntityZ(Collider))			
							n\state = 1
					End Select
					
				Default
					CreateConsoleMsg("Command not found")
			End Select
			
			ConsoleInput = ""
		End If
		
		Local TempY% = y + height - 70
		Local cm.ConsoleMsg
		For cm.ConsoleMsg = Each ConsoleMsg
			If TempY < y + 20 Then
				Delete cm
			Else
				Text(x + 30, TempY, cm\txt)
				TempY = TempY - 15
			EndIf
		Next
	End If
	
End Function

CreateConsoleMsg("Console commands: ")
CreateConsoleMsg("  - teleport [room name] ")
CreateConsoleMsg("  - godmode on/off")
CreateConsoleMsg("  - noclip on/off")
CreateConsoleMsg("  - spawnitem [item name]")
CreateConsoleMsg("  - heal")
CreateConsoleMsg("  - 173speed [x] (default = 35)")
CreateConsoleMsg("  - disable173/enable173")
CreateConsoleMsg("  - disable106/enable106")
CreateConsoleMsg("  - 173state/106state/096state")
CreateConsoleMsg("  - status")
CreateConsoleMsg("  - wireframe on/off")
CreateConsoleMsg("  - noclipspeed [x] (default = 2.0)")
CreateConsoleMsg("  - debughud on/off")
CreateConsoleMsg("  - camerafog [near] [far]")
CreateConsoleMsg("  - spawn [npc type]")
CreateConsoleMsg(" I would heavily reccomend that you don't use console commands.")
CreateConsoleMsg(" Most of the time they will casue server pointers to de-sync.")

;---------------------------------------------------------------------------------------------------

Global DebugHUD%

Global BlurVolume#

Global LightBlink#, LightFlash#

Global BumpEnabled% = GetINIInt("options.ini", "options", "bump mapping enabled")
Global HUDenabled% = GetINIInt("options.ini", "options", "HUD enabled")

;Local light = createLight()
Global Camera%, CameraShake#

Global Brightness% = 40;Max(Min(GetINIInt("options.ini", "options", "brightness"), 255), 0)
Global CameraFogNear# = GetINIFloat("options.ini", "options", "camera fog near")
Global CameraFogFar# = GetINIFloat("options.ini", "options", "camera fog far")

Global CurrCameraZoom#

Global MouseSens# = GetINIFloat("options.ini", "options", "mouse sensitivity")

Global BlurTimer#
Include "dreamfilter.bb"

Dim LightSpriteTex(10)

;----------------------------------------------  Network -----------------------------------------------------
Type Client ;Mp mod
	Field stream
	Field name$
	Field ready%
	
	Field Obj
	Field Collider
	
	Field Crouch
	
	Field x#,y#,z#,yaw#
	Field lx#,ly#,lz#,lyaw#
	Field ft%,lft%
	
	Field currspeedT#,dir%
End Type

Const MpMaxClients = 3
Global MpAnimSpeed# = 4
Global CurrPort$ = "25550" ;Mp mod
Global CurrIp$ = "127.0.0.1"
Global CurrName$ = "Player"
Global MpMyID% = 0

Global MpConn ;Mp mod
Global MpStream
Global MpNumClients
Dim MpClients.Client(MpMaxClients)
Global mpState% ;Mp mod
;----------------------------------------------  Sounds -----------------------------------------------------

;[Block]

Global SoundEmitter%, TempSound%, TempSound2%

Dim Music%(8)
Music(0) = LoadSound("SFX\Music\The Dread.ogg")
Music(1) = LoadSound("SFX\Music\Bump in the Night.ogg")
Music(2) = LoadSound("SFX\Music\MenuAmbience.ogg")
;Music(3) = LoadSound("SFX\Ambient\PocketDimension.ogg")
;Music(4) = LoadSound("SFX\Music\AI.ogg")
;Music(5) = LoadSound("SFX\Music\Satiate Strings.ogg")
;Music(6) = LoadSound("SFX\Music\Medusa.ogg")

Global MusicVolume# = GetINIFloat(OptionFile, "options", "music volume")
Global MusicCHN% = PlaySound(Music(2))
ChannelVolume(MusicCHN, MusicVolume)
Global CurrMusicVolume#, NowPlaying%, ShouldPlay%

DrawLoading(10, True)

Dim OpenDoorSFX%(3), CloseDoorSFX%(3)
For i = 0 To 2
	OpenDoorSFX(i) = LoadSound("SFX\DoorOpen" + (i + 1) + ".ogg")
	CloseDoorSFX(i) = LoadSound("SFX\DoorClose" + (i + 1) + ".ogg")
Next
Global OpenDoorFastSFX=LoadSound("SFX\DoorOpenFast.ogg")
Global CautionSFX% = LoadSound("SFX\caution.ogg")

Global NuclearSirenSFX%

Global BigDoorCloseSFX% = LoadSound("SFX\bigdoorclose.ogg"), BigDoorOpenSFX% = LoadSound("SFX\bigdooropen.ogg")

Global StoneDragSFX% = LoadSound("SFX\StoneDrag.ogg")

Global GunshotSFX% = LoadSound("SFX\gunshot.ogg"),Gunshot2SFX% = LoadSound("SFX\gunshot2.ogg"),Gunshot3SFX% = LoadSound("SFX\bulletmiss.ogg")
Global BullethitSFX% = LoadSound("SFX\bullethit.ogg")

Global Msg079%

Global TeslaIdleSFX = LoadSound("SFX\teslaidle.ogg"), TeslaActivateSFX = LoadSound("SFX\teslaactivate.ogg")
Global TeslaPowerUpSFX = LoadSound("SFX\teslapowerup.ogg")

Global MagnetUpSFX% = LoadSound("SFX\MagnetUp.ogg"), MagnetDownSFX = LoadSound("SFX\MagnetDown.ogg")
Global FemurBreakerSFX%

Dim DecaySFX%(5)
For i = 0 To 3
	DecaySFX(i) = LoadSound("SFX\decay" + i + ".ogg")
Next

Global BurstSFX = LoadSound("SFX\burst.ogg")

DrawLoading(20, True)

Dim RustleSFX%(3)
For i = 0 To 2
	RustleSFX(i) = LoadSound("SFX\rustle" + i + ".ogg")
Next

Global Death914SFX% = LoadSound("SFX\914death.ogg"), Use914SFX% = LoadSound("SFX\914use.ogg")

Dim DripSFX%(4)
For i = 0 To 3
	DripSFX(i) = LoadSound("SFX\drip" + i + ".ogg")
Next

Global LeverSFX% = LoadSound("SFX\lever.ogg"), LightSFX% = LoadSound("SFX\lightswitch.ogg")

Global GasmaskBreathCHN%, GasmaskBreath% = LoadSound("SFX\GasmaskBreath.ogg")

Global ButtGhostSFX% = LoadSound("SFX\BuGh.ogg")

Dim RadioSFX(5,10)
RadioSFX(1,0) = LoadSound("SFX\Radio\RadioAlarm.ogg")
RadioSFX(1,1) = LoadSound("SFX\Radio\RadioAlarm2.ogg")
For i = 0 To 8
	RadioSFX(2,i) = LoadSound("SFX\Radio\scpradio"+i+".ogg")
Next
Global RadioSquelch = LoadSound("SFX\Radio\squelch.ogg")
Global RadioStatic = LoadSound("SFX\Radio\static.ogg")
Global RadioBuzz = LoadSound("SFX\Radio\buzz.ogg")

Global ElevatorBeepSFX = LoadSound("SFX\ElevatorBeep.ogg"), ElevatorMoveSFX = LoadSound("SFX\ElevatorMove.ogg") 

Dim PickSFX%(10)
For i = 0 To 2
	PickSFX(i) = LoadSound("SFX\PickItem" + i + ".ogg")
Next

Global AmbientSFXAmount% = 19, AmbientSFXCHN%, CurrAmbientSFX%
Dim AmbientSFX%(AmbientSFXAmount)
;For i = 0 To 10;(AmbientSFXAmount - 1)
;	AmbientSFX(i) = LoadSound("SFX\Ambient\Ambient" + (i + 1) + ".ogg")
;Next

Dim OldManSFX%(6)
For i = 0 To 4
	OldManSFX(i) = LoadSound("SFX\oldman" + (i + 1) + ".ogg")
Next
OldManSFX(5) = LoadSound("SFX\oldmandrag.ogg")

Dim Scp173SFX%(3)
For i = 0 To 2
	Scp173SFX(i) = LoadSound("SFX\173sound" + (i + 1) + ".ogg")
Next

Dim HorrorSFX%(20)
For i = 0 To 10
	HorrorSFX(i) = LoadSound("SFX\horror" + (i + 1) + ".ogg")
Next

DrawLoading(25, True)

Dim IntroSFX%(16)

For i = 7 To 9
	IntroSFX(i) = LoadSound("SFX\intro\bang" + (i - 6) + ".ogg")
Next
For i = 10 To 12
	IntroSFX(i) = LoadSound("SFX\intro\elec" + (i - 9) + ".ogg")
Next
IntroSFX(13) = LoadSound("SFX\intro\shoot1.ogg")
IntroSFX(14) = LoadSound("SFX\intro\shoot2.ogg")
IntroSFX(15) = LoadSound("SFX\intro\metal173.ogg")

Dim AlarmSFX%(5)
AlarmSFX(0) = LoadSound("SFX\alarm.ogg")
AlarmSFX(1) = LoadSound("SFX\alarm2.ogg")
AlarmSFX(2) = LoadSound("SFX\alarm3.ogg")


Dim DamageSFX%(5)
For i = 0 To 2
	DamageSFX(i) = LoadSound("SFX\NeckSnap"+(i+1)+".ogg")
Next

Global HeartBeatSFX = LoadSound("SFX\heartbeat.ogg")

Dim DeathSFX%(6)
For i = 0 To 4
	DeathSFX(i) = LoadSound("SFX\death"+(i+1)+".ogg")
Next

Dim MTFSFX%(10)

Global Roar682SFX
Global GlassBreakSFX = LoadSound("SFX\glassbreak.ogg") 

Dim CoughSFX%(3)
Global CoughCHN%
For i = 0 To 2
	CoughSFX(i) = LoadSound("SFX\cough" + (i + 1) + ".ogg")
Next

Global MachineSFX% = LoadSound("SFX\Machine.ogg")

Global ApacheSFX = LoadSound("SFX\apache.ogg")

Dim StepSFX%(3, 2, 4) ;(normal/metal, walk/run, id)
For i = 0 To 3
	StepSFX(0, 0, i) = LoadSound("SFX\step" + (i + 1) + ".ogg")
	StepSFX(1, 0, i) = LoadSound("SFX\stepmetal" + (i + 1) + ".ogg")
	StepSFX(0, 1, i)= LoadSound("SFX\run" + (i + 1) + ".ogg")
	StepSFX(1, 1, i) = LoadSound("SFX\runmetal" + (i + 1) + ".ogg")
	If i < 3 Then StepSFX(2, 0, i) = LoadSound("SFX\MTF\StepMTF" + (i + 1) + ".ogg")	
Next

Dim StepPDSFX(4)
For i = 0 To 2
	StepPDSFX(i) = LoadSound("SFX\stepPD" + (i + 1) + ".ogg")
Next 

DrawLoading(30, True)

;[End block]

;-----------------------------------------  Images ----------------------------------------------------------

Global PauseMenuIMG% = LoadImage("GFX\menu\pausemenu.jpg")
MaskImage PauseMenuIMG, 255,255,0

Global SprintIcon% = LoadImage("GFX\sprinticon.png"), BlinkIcon% = LoadImage("GFX\blinkicon.png"), CrouchIcon% = LoadImage("GFX\sneakicon.png")
Global HandIcon% = LoadImage("GFX\handsymbol.png")

Global StaminaMeterIMG% = LoadImage("GFX\staminameter.jpg")

Global SignIMG% = LoadImage("GFX\map\sign.jpg")

Global KeypadHUD =  LoadImage("GFX\keypadhud.jpg")
MaskImage(KeypadHUD, 255,0,255)
Global ButtonUp =  LoadImage("GFX\buttonup.png"), ButtonDown =  LoadImage("GFX\buttondown.png")

DrawLoading(35, True)

;----------------------------------------------  Items  -----------------------------------------------------

Include "Items.bb"

;--------------------------------------- Particles ------------------------------------------------------------

Include "Particles.bb"

;-------------------------------------  Doors --------------------------------------------------------------

Global ClosestButton%, ClosestDoor.Doors
Global SelectedDoor.Doors, UpdateDoorsTimer#
Global DoorTempID%
Type Doors
	Field obj%, obj2%, frameobj%, buttons%[2]
	Field locked%, open%, angle%, openstate#, fastopen%
	Field dir%
	Field timer%, timerstate#
	Field KeyCard%
	Field room.Rooms
	
	Field DisableWaypoint%
	
	Field dist#
	
	Field SoundCHN%
	
	Field Code$
	
	Field ID%
	
	Field Level%
	Field LevelDest%
	
	Field AutoClose%
	
	Field LinkedDoor.Doors
End Type 

Dim BigDoorOBJ(2), HeavyDoorObj(2)

Function CreateDoor.Doors(lvl, x#, y#, z#, angle#, room.Rooms, dopen% = False,  big% = False, keycard% = False, code$="")
	Local d.Doors, parent, i%
	If room <> Null Then parent = room\obj
	
	d.Doors = New Doors
	If big=1 Then
		d\obj = CopyMesh(BigDoorOBJ(0))
		ScaleEntity(d\obj, 55 * RoomScale, 55 * RoomScale, 55 * RoomScale)
		d\obj2 = CopyMesh(BigDoorOBJ(1))
		ScaleEntity(d\obj2, 55 * RoomScale, 55 * RoomScale, 55 * RoomScale)
		
		d\frameobj = CopyMesh(DoorColl)					
		ScaleEntity(d\frameobj, RoomScale, RoomScale, RoomScale)
		EntityType d\frameobj, HIT_MAP
		EntityAlpha d\frameobj, 0.0
	ElseIf big=2
		d\obj = CopyMesh(HeavyDoorObj(0))
		ScaleEntity(d\obj, RoomScale, RoomScale, RoomScale)
		d\obj2 = CopyMesh(HeavyDoorObj(1))
		ScaleEntity(d\obj2, RoomScale, RoomScale, RoomScale)
		
		d\frameobj = CopyMesh(DoorFrameOBJ)
	Else
		d\obj = CopyMesh(DoorOBJ)
		ScaleEntity(d\obj, (204.0 * RoomScale) / MeshWidth(d\obj), 312.0 * RoomScale / MeshHeight(d\obj), 16.0 * RoomScale / MeshDepth(d\obj))
		
		d\frameobj = CopyMesh(DoorFrameOBJ)
		d\obj2 = CopyMesh(DoorOBJ)
		
		ScaleEntity(d\obj2, (204.0 * RoomScale) / MeshWidth(d\obj), 312.0 * RoomScale / MeshHeight(d\obj), 16.0 * RoomScale / MeshDepth(d\obj))
		;entityType d\obj2, HIT_MAP
	End If
	
	d\ID = DoorTempID
	DoorTempID=DoorTempID+1
	
	;scaleentity(d\obj, 0.1, 0.1, 0.1)
	ScaleEntity(d\frameobj, (8.0 / 2048.0), (8.0 / 2048.0), (8.0 / 2048.0))
	EntityType d\obj, HIT_MAP
	EntityType d\obj2, HIT_MAP
	
	d\KeyCard = keycard
	d\Code = code
	
	d\Level = lvl
	d\LevelDest = 66
	
	For i% = 0 To 1
		If code <> "" Then 
			d\buttons[i]= CopyMesh(ButtonCodeOBJ)
		Else
			If keycard Then
				d\buttons[i]= CopyMesh(ButtonKeyOBJ)
			Else
				d\buttons[i] = CopyMesh(ButtonOBJ)
			End If
		EndIf
		
		ScaleEntity(d\buttons[i], 0.03, 0.03, 0.03)
	Next
	
	If big=1 Then
		PositionEntity d\buttons[0], x - 432.0 * RoomScale, y + 0.7, z + 192.0 * RoomScale
		PositionEntity d\buttons[1], x + 432.0 * RoomScale, y + 0.7, z - 192.0 * RoomScale
		RotateEntity d\buttons[0], 0, 90, 0
		RotateEntity d\buttons[1], 0, 270, 0
	Else
		PositionEntity d\buttons[0], x + 0.6, y + 0.7, z - 0.1
		PositionEntity d\buttons[1], x - 0.6, y + 0.7, z + 0.1
		RotateEntity d\buttons[1], 0, 180, 0		
	End If
	
	PositionEntity d\obj, x, y, z
	If d\obj2 <> 0 Then
		PositionEntity d\obj2, x, y, z
		If big=1 Then
			RotateEntity(d\obj2, 0, angle, 0)
		Else
			RotateEntity(d\obj2, 0, angle + 180, 0)
		EndIf
		EntityParent(d\obj2, parent)			
	EndIf
	PositionEntity d\frameobj, x, y, z
	
	EntityParent(d\frameobj, parent)
	EntityParent(d\obj, parent)
	
	EntityParent(d\buttons[0], d\frameobj)
	EntityParent(d\buttons[1], d\frameobj)
	
	RotateEntity d\obj, 0, angle, 0
	RotateEntity d\frameobj, 0, angle, 0	
	d\angle = angle
	d\open = dopen
	
	EntityPickMode(d\obj, 3)
	MakeCollBox(d\obj)
	If d\obj2 <> 0 Then
		EntityPickMode(d\obj2, 3)
		MakeCollBox(d\obj2)
	End If
	
	EntityPickMode(d\buttons[0], 2)
	EntityPickMode(d\buttons[1], 2)
	
	If d\open And big = False And Rand(8) = 1 Then d\AutoClose = True
	
	d\dir=big
	
	d\room = room
	
	Return d
	
End Function

Function CreateButton(x#,y#,z#, pitch#,yaw#,roll#=0)
	Local obj = CopyMesh(ButtonOBJ)	
	
	ScaleEntity(obj, 0.03, 0.03, 0.03)
	
	PositionEntity obj, x,y,z
	RotateEntity obj, pitch,yaw,roll
	
	EntityPickMode(obj, 2)	
	
	Return obj
End Function

Function UpdateDoors()
	
	Local i%, d.Doors, x#, z#
	If UpdateDoorsTimer =< 0 Then
		For d.Doors = Each Doors
			
			Local xdist# = Abs(EntityX(Collider)-EntityX(d\obj,True))
			Local zdist# = Abs(EntityX(Collider)-EntityX(d\obj,True))
			If xdist+zdist > 32.0 Then
				d\dist = xdist+zdist
			Else
				d\dist = EntityDistance(Collider,d\obj)
			EndIf
			
			If d\dist > HideDistance*2 Then
				If d\obj <> 0 Then HideEntity d\obj
				If d\frameobj <> 0 Then HideEntity d\frameobj
				If d\obj2 <> 0 Then HideEntity d\obj2
				If d\buttons[0] <> 0 Then HideEntity d\buttons[0]
				If d\buttons[1] <> 0 Then HideEntity d\buttons[1]				
			Else
				If d\obj <> 0 Then ShowEntity d\obj
				If d\frameobj <> 0 Then ShowEntity d\frameobj
				If d\obj2 <> 0 Then ShowEntity d\obj2
				If d\buttons[0] <> 0 Then ShowEntity d\buttons[0]
				If d\buttons[1] <> 0 Then ShowEntity d\buttons[1]							
			EndIf
			
		Next
		
		UpdateDoorsTimer = 30
	Else
		UpdateDoorsTimer = Max(UpdateDoorsTimer-FPSfactor,0)
	EndIf
	
	ClosestButton = 0
	ClosestDoor = Null
	
	For d.Doors = Each Doors
		If d\dist < HideDistance*2 Then 
			
			If (d\openstate >= 180 Or d\openstate <= 0) And GrabbedEntity = 0 Then
				For i% = 0 To 1
					If d\buttons[i] <> 0 Then
						If Abs(EntityX(Collider)-EntityX(d\buttons[i],True)) < 1.0 Then 
							If Abs(EntityZ(Collider)-EntityZ(d\buttons[i],True)) < 1.0 Then 
								Local dist# = Distance(EntityX(Collider, True), EntityZ(Collider, True), EntityX(d\buttons[i], True), EntityZ(d\buttons[i], True));entityDistance(collider, d\buttons[i])
								If dist < 0.7 Then
									Local temp% = CreatePivot()
									PositionEntity temp, EntityX(Camera), EntityY(Camera), EntityZ(Camera)
									PointEntity temp,d\buttons[i]
									
									If EntityPick(temp, 0.6) = d\buttons[i] Then
										If ClosestButton = 0 Then
											ClosestButton = d\buttons[i]
											ClosestDoor = d
										Else
											If dist < EntityDistance(Collider, ClosestButton) Then ClosestButton = d\buttons[i] : ClosestDoor = d
										End If							
									End If
									
									FreeEntity temp
									
								EndIf							
							EndIf
						EndIf
						
					EndIf
				Next
			EndIf
			
			If d\open Then
				If d\openstate < 180 Then
					Select d\dir
						Case 0
							d\openstate = Min(180, d\openstate + FPSfactor * 2 * (d\fastopen+1))
							MoveEntity(d\obj, Sin(d\openstate) * (d\fastopen+1) * FPSfactor / 80.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, Sin(d\openstate)* (d\fastopen+1) * FPSfactor / 80.0, 0, 0)		
						Case 1
							d\openstate = Min(180, d\openstate + FPSfactor * 0.8)
							MoveEntity(d\obj, Sin(d\openstate) * FPSfactor / 180.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, -Sin(d\openstate) * FPSfactor / 180.0, 0, 0)	
						Case 2
							d\openstate = Min(180, d\openstate + FPSfactor * 2 * (d\fastopen+1))
							MoveEntity(d\obj, Sin(d\openstate) * (d\fastopen+1) * FPSfactor / 85.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, Sin(d\openstate)* (d\fastopen+1) * FPSfactor / 120.0, 0, 0)		
					End Select
				Else
					d\fastopen = 0
					ResetEntity(d\obj)
					If d\obj2 <> 0 Then ResetEntity(d\obj2)
					If d\timer > 0 And d\timerstate > 0 Then
						d\timerstate = Max(0, d\timerstate - FPSfactor)
						If d\timerstate + FPSfactor > 110 And d\timerstate <= 110 Then PlaySound2(CautionSFX, Camera, d\obj)
						If d\timerstate = 0 Then d\open = (Not d\open) : PlaySound2(CloseDoorSFX(Rand(0, 2)), Camera, d\obj)
					EndIf
					If d\AutoClose And RemoteDoorOn = True Then
						If EntityDistance(Camera, d\obj) < 2.1 Then
							If (Not Wearing714) Then PlaySound HorrorSFX(7)
							d\open = False : PlaySound2(CloseDoorSFX(Rand(0, 2)), Camera, d\obj) : d\AutoClose = False
						EndIf
					End If				
				End If
			Else
				If d\openstate > 0 Then
					Select d\dir
						Case 0
							d\openstate = Max(0, d\openstate - FPSfactor * 2 * (d\fastopen+1))
							MoveEntity(d\obj, Sin(d\openstate) * -FPSfactor * (d\fastopen+1) / 80.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, Sin(d\openstate) * (d\fastopen+1) * -FPSfactor / 80.0, 0, 0)	
						Case 1
							d\openstate = Max(0, d\openstate - FPSfactor*0.8)
							MoveEntity(d\obj, Sin(d\openstate) * -FPSfactor / 180.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, Sin(d\openstate) * FPSfactor / 180.0, 0, 0)
						Case 2
							d\openstate = Max(0, d\openstate - FPSfactor * 2 * (d\fastopen+1))
							MoveEntity(d\obj, Sin(d\openstate) * -FPSfactor * (d\fastopen+1) / 85.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, Sin(d\openstate) * (d\fastopen+1) * -FPSfactor / 120.0, 0, 0)	
					End Select
					
					If d\angle = 0 Or d\angle=180 Then
						If Abs(EntityZ(d\frameobj, True)-EntityZ(Collider))<0.15 Then
							If Abs(EntityX(d\frameobj, True)-EntityX(Collider))<0.7*(d\dir*2+1) Then
								z# = CurveValue(EntityZ(d\frameobj,True)+0.15*Sgn(EntityZ(Collider)-EntityZ(d\frameobj, True)), EntityZ(Collider), 5)
								PositionEntity Collider, EntityX(Collider), EntityY(Collider), z
							EndIf
						EndIf
					Else
						If Abs(EntityX(d\frameobj, True)-EntityX(Collider))<0.15 Then	
							If Abs(EntityZ(d\frameobj, True)-EntityZ(Collider))<0.7*(d\dir*2+1) Then
								x# = CurveValue(EntityX(d\frameobj,True)+0.15*Sgn(EntityX(Collider)-EntityX(d\frameobj, True)), EntityX(Collider), 5)
								PositionEntity Collider, x, EntityY(Collider), EntityZ(Collider)
							EndIf
						EndIf
					EndIf
					
				Else
					d\fastopen = 0
					PositionEntity(d\obj, EntityX(d\frameobj, True), EntityY(d\frameobj, True), EntityZ(d\frameobj, True))
					If d\obj2 <> 0 Then PositionEntity(d\obj2, EntityX(d\frameobj, True), EntityY(d\frameobj, True), EntityZ(d\frameobj, True))
					If d\obj2 <> 0 And d\dir = 0 Then
						MoveEntity(d\obj, 0, 0, 8.0 * RoomScale)
						MoveEntity(d\obj2, 0, 0, 8.0 * RoomScale)
					EndIf	
				End If
			End If
			
		EndIf
		
	Next
End Function

Function UseDoor(d.Doors, showmsg%=True, netcall% = True)
	Local offset% = 0
	If netcall = True
	
		DebugLog d\timer + " - "+d\timerstate
		
		If d\KeyCard > 0 Then
			If SelectedItem = Null Then
				If showmsg = True Then 
					Msg = "You need a key card to operate the door"
					MsgTimer = 70 * 5
				EndIf
				Return
			Else
				Local temp% = 0
				Select SelectedItem\itemtemplate\tempname
					Case "key1"
						temp = 1
					Case "key2"
						temp = 2
					Case "key3"
						temp = 3
					Case "key4"
						temp = 4
					Case "key5"
						temp = 5
					Case "key6"
						temp = 6
					Default 
						temp = -1
				End Select
				
				If temp =-1 Then 
					If showmsg = True Then 
						Msg = "You need a key card to operate the door"
						MsgTimer = 70 * 5
					EndIf
					Return				
				ElseIf temp >= d\KeyCard 
					If showmsg = True Then 
						Msg = "You inserted the key card into the slot"
						MsgTimer = 70 * 5
					EndIf
					SelectedItem = Null
				Else
					If showmsg = True Then 
						Msg = "You need a key card with a higher security clearance to operate the door"
						MsgTimer = 70 * 5
						SelectedItem = Null
					EndIf
					Return
				End If
				
				
			EndIf
		Else
			If d\locked Then
				If showmsg = True Then 
					Msg = "It seems to be locked"
					MsgTimer = 70 * 5
				EndIf
				Return
			Else
				
			End If
			
		End If
	End If
	
	If d\LevelDest <> 66 Then 
		Local CurrLevel = PlayerLevel
		Local DestLevel = d\LevelDest
		Local TargetX#,TargetY#,TargetZ#
		
		For d2.doors = Each Doors
			If d2\leveldest = CurrLevel Then
				PointEntity(Collider, d\obj)
				RotateEntity(Collider, 0, EntityYaw(Collider),0)
				PositionEntity Collider, EntityX(d2\obj),EntityY(d2\obj),EntityZ(d2\obj)
				MoveEntity Collider, 0,0,0.4
				TargetX = EntityX(Collider)
				TargetY = EntityX(Collider)
				TargetZ = EntityX(Collider)	
			EndIf
		Next 		
		
		SaveGame(SavePath + CurrSave + "\")
		NullGame()
		
		PlayerLevel = DestLevel		
		LoadEntities()
		LoadGame(SavePath + CurrSave + "\")
		InitLoadGame()
		
		PlayerLevel = DestLevel
		
		Return
	Else
	
		d\open = (Not d\open)
		If d\LinkedDoor <> Null Then d\LinkedDoor\open = (Not d\LinkedDoor\open)
		If d\open Then
			If d\LinkedDoor <> Null Then d\LinkedDoor\timerstate = d\LinkedDoor\timer
			d\timerstate = d\timer
			If d\dir = 1 Then
				d\SoundCHN = PlaySound2 (BigDoorOpenSFX, Camera, d\obj)
			Else
				d\SoundCHN = PlaySound2 (OpenDoorSFX(Rand(0, 2)), Camera, d\obj)
			EndIf
		Else
			If d\dir = 1 Then
				d\SoundCHN = PlaySound2 (BigDoorCloseSFX, Camera, d\obj)
			Else
				d\SoundCHN = PlaySound2 (CloseDoorSFX(Rand(0, 2)), Camera, d\obj)
			EndIf
		End If
		
	EndIf
	If netcall = True
		For dt.Doors = Each Doors
			If dt = d
				Exit
			EndIf
			offset = offset + 1
		Next
		
		If mpState = 1
			For i% = 1 To MpNumClients - 1
				WriteLine MpClients(i%)\stream, "doorupdate"
				WriteLine MpClients(i%)\stream, offset
			Next
		EndIf
		
		If mpState = 2 Then
			WriteLine MpStream,"doorupdate"
			WriteLine MpStream,offset
			
		EndIf
	EndIf

End Function

Function RemoveDoor(d.Doors)
	If d\buttons[0] <> 0 Then EntityParent d\buttons[0], 0
	If d\buttons[1] <> 0 Then EntityParent d\buttons[1], 0	
	
	If d\obj <> 0 Then FreeEntity d\obj
	If d\obj2 <> 0 Then FreeEntity d\obj2
	If d\frameobj <> 0 Then FreeEntity d\frameobj
	If d\buttons[0] <> 0 Then FreeEntity d\buttons[0]
	If d\buttons[1] <> 0 Then FreeEntity d\buttons[1]	
	
	Delete d
End Function

DrawLoading(40,True)

Include "MapSystem.bb"

DrawLoading(80,True)

Include "NPCs.bb"

;-------------------------------------  Events --------------------------------------------------------------

Type Events
	Field EventName$
	Field room.Rooms
	Field EventState#, EventState2#, EventState3#
	Field SoundCHN%, SoundCHN2%
	Field Sound, Sound2
	Field img%
End Type 
	
Function CreateEvent.Events(eventname$, roomname$, id%, prob# = 0.0)
	;roomname = the name of the room(s) you want the event to be assigned to
	
	;the id-variable determines which of the rooms the event is assigned to,
	;0 will assign it to the first generated room, 1 to the second, etc
	
	;the prob-variable can be used to randomly assign events into some rooms
	;0.5 means that there's a 50% chance that event is assigned to the rooms
	;1.0 means that the event is assigned to every room
	;the id-variable is ignored if prob <> 0.0
	
	Local i% = 0, n% = 0, temp%, e.Events, e2.Events, r.Rooms
	
	If prob = 0.0 Then
		For r.Rooms = Each Rooms
			n=n+1
			If (roomname = "" Or roomname = r\RoomTemplate\Name) Then
				
				temp = False
				For e2.Events = Each Events
					If e2\room = r Then temp = True : Exit
				Next
				
				i=i+1
				If i >= id And temp = False Then
					e.Events = New Events
					e\EventName = eventname					
					e\room = r
					Return e
				End If
			EndIf
		Next
	Else
		For r.Rooms = Each Rooms
			If (roomname = "" Or roomname = r\RoomTemplate\Name) Then
				temp = False
				For e2.Events = Each Events
					If e2\room = r Then temp = True : Exit
				Next
				
				If Rnd(0.0, 1.0) < prob And temp = False Then
					e.Events = New Events
					e\EventName = eventname					
					e\room = r
				End If
			EndIf
		Next		
	EndIf
	
	Return Null
End Function

Function InitEvents()
	Local e.Events
	
	CreateEvent("173", "173", 0)
	CreateEvent("alarm", "start", 0)
	
	CreateEvent("pocketdimension", "pocketdimension", 0)	
	
	;there's a 7% chance that 106 appears in the rooms named "tunnel"
	CreateEvent("tunnel106", "tunnel", 0, 0.07)
	
	;the chance for 173 appearing in the first lockroom is about 66%
	;there's a 30% chance that it appears in the later lockrooms
	If Rand(3)<3 Then CreateEvent("lockroom173", "lockroom", 0)
	CreateEvent("lockroom173", "lockroom", 0, 0.3)
	
	;096 spawns in the first (and last) lockroom2
	CreateEvent("lockroom096", "lockroom2", 0)	
	
	CreateEvent("tunnel2smoke", "tunnel2", 0, 0.2)		
	CreateEvent("tunnel2", "tunnel2", Rand(0,2), 0)
	
	;173 appears in half of the "room2doors" -rooms
	CreateEvent("room2doors173", "room2doors", 0, 0.5)
	
	;the anomalous duck in room2offices2-rooms
	CreateEvent("room2offices2", "room2offices2", 0, 0.7)
	
	CreateEvent("room2closets", "room2closets", 0)	
	
	CreateEvent("room3pit", "room3pit", 0)
	
	;the event that causes the door to open by itself in room2offices3
	CreateEvent("room2offices3", "room2offices3", 0, 1.0)	
	
	CreateEvent("room2servers", "room2servers", 0)	
	
	CreateEvent("room3servers", "room3servers", 0)	
	CreateEvent("room3servers", "room3servers2", 0)
	
	;the dead guard
	CreateEvent("room3tunnel","room3tunnel", 0, 0.08)
	
	If Rand(5)<5 Then 
		Select Rand(3)
			Case 1
				CreateEvent("682roar", "tunnel", Rand(0,2), 0)	
			Case 2
				CreateEvent("682roar", "room3pit", Rand(0,2), 0)		
			Case 3
				CreateEvent("682roar", "room2offices", 0, 0)	
		End Select 
	EndIf 
	
	CreateEvent("testroom173", "room2testroom2", 0, 1.0)	
	
	CreateEvent("room2tesla", "room2tesla", 0, 0.9)	
	
	e = CreateEvent("room2nuke", "room2nuke", 0, 0)	
	If e <> Null Then e\EventState = 1
	
	If Rand(5) < 5 Then 
		CreateEvent("coffin106", "coffin", 0, 0)
	Else
		CreateEvent("coffin", "coffin", 0, 0)		
	EndIf 
	
	CreateEvent("room079", "room079", 0, 0)	
	
	CreateEvent("room049", "room049", 0, 0)
	
	CreateEvent("room012", "room012", 0, 0)
	
	CreateEvent("008", "008", 0, 0)
	
	e.Events = CreateEvent("room106", "room106", 0, 0)	
	If e <> Null Then e\EventState2 = 1
	
	CreateEvent("pj", "roompj", 0, 0)
	
	CreateEvent("914", "914", 0, 0)
	
	CreateEvent("toiletguard", "room2toilets", 1)
	CreateEvent("buttghost", "room2toilets", 0, 0.8)
	
	CreateEvent("room2pipes106", "room2pipes", Rand(0, 3)) 
	
	CreateEvent("room2pit", "room2pit", 0, 0.4)
	
	CreateEvent("testroom", "testroom", 0)
	
	CreateEvent("room2tunnel", "room2tunnel", 0)
	
	CreateEvent("room2ccont", "room2ccont", 0)
	
	CreateEvent("gateaentrance", "gateaentrance", 0)	
	CreateEvent("gatea", "gatea", 0)	
	CreateEvent("exit1", "exit1", 0)
	
	;CreateEvent("forest", "forest", 0)	
	
End Function

Function UpdateEvents()
	Local dist#, i%, temp%, pvt%
	
	Local p.Particles, n.NPCs, r.Rooms, e.Events, e2.Events, it.Items, em.Emitters
	
	Local angle#
	
	;PlayerRoom = Null
	For r.Rooms = Each Rooms
		If Abs(EntityX(Collider) - EntityX(r\obj)) < 12.0 Then 
			If Abs(EntityZ(Collider) - EntityZ(r\obj)) < 12.0 Then
				MapFound(PlayerLevel, Floor(EntityX(r\obj) / 8.0), Floor(EntityZ(r\obj) / 8.0)) = Max(MapFound(PlayerLevel, Floor(EntityX(r\obj) / 8.0), Floor(EntityZ(r\obj) / 8.0)), 1)
				If Abs(EntityX(Collider) - EntityX(r\obj)) < 4.0 Then
					If Abs(EntityZ(Collider) - EntityZ(r\obj)) < 4.0 Then
						If Abs(EntityY(Collider) - EntityY(r\obj)) < 1.5 Then PlayerRoom = r
						MapFound(PlayerLevel, Floor(EntityX(r\obj) / 8.0), Floor(EntityZ(r\obj) / 8.0)) = 2
					EndIf
				EndIf
			EndIf
		End If
	Next
	
	
	For i = 0 To 3
		If PlayerRoom\SoundEmitter[i]<>0 Then 
			dist# = EntityDistance(PlayerRoom\SoundEmitterObj[i],Collider)
			If dist < PlayerRoom\SoundEmitterRange[i] Then
				DebugLog dist
				PlayerRoom\SoundEmitterCHN[i] = LoopSound2(RoomAmbience[PlayerRoom\SoundEmitter[i]],PlayerRoom\SoundEmitterCHN[i], Camera, PlayerRoom\SoundEmitterObj[i],PlayerRoom\SoundEmitterRange[i],1.5)
			EndIf
		EndIf
	Next
	
	;If PlayerRoom\RoomTemplate\ambience<>0 Then 
		;PlayerRoom\SoundCHN = LoopSound2(RoomAmbience[PlayerRoom\RoomTemplate\ambience], PlayerRoom\SoundCHN, Camera, PlayerRoom\obj, 4.0)
		
	;	dist# = Distance(EntityX(Collider),EntityZ(Collider),EntityX(PlayerRoom\obj),EntityZ(PlayerRoom\obj))
	;	If dist < 4.0 Then
	;		If PlayerRoom\SoundCHN = 0 Then
	;			PlayerRoom\SoundCHN = PlaySound (RoomAmbience[PlayerRoom\RoomTemplate\ambience])
	;		Else
	;			If (Not ChannelPlaying(PlayerRoom\SoundCHN)) Then PlayerRoom\SoundCHN = PlaySound (RoomAmbience[PlayerRoom\RoomTemplate\ambience])
	;		EndIf
	;		
	;		ChannelVolume(PlayerRoom\SoundCHN, Min((4.0 - dist#) / 2.0, 1.0))
	;	EndIf
	;EndIf 
	
	For e.Events = Each Events
		Select e\EventName
			Case "alarm" ;the alarm in the starting room
				If e\EventState = 0 Then
					If PlayerRoom = e\room Then
						ShowEntity Fog
						AmbientLight Brightness, Brightness, Brightness
						CameraFogRange(Camera, CameraFogNear, CameraFogFar)
						CameraFogMode(Camera, 1)
						
						While e\room\RoomDoors[1]\openstate < 180
							e\room\RoomDoors[1]\openstate = Min(180, e\room\RoomDoors[1]\openstate + 0.8)
							MoveEntity(e\room\RoomDoors[1]\obj, Sin(e\room\RoomDoors[1]\openstate) / 180.0, 0, 0)
							MoveEntity(e\room\RoomDoors[1]\obj2, -Sin(e\room\RoomDoors[1]\openstate) / 180.0, 0, 0)
						Wend
						
						If e\room\NPC[0] <> Null Then SetAnimTime e\room\NPC[0]\obj, 10
						
						e\EventState = 1
					EndIf
				Else
					If e\room\NPC[0] <> Null Then Animate2(e\room\NPC[0]\obj, AnimTime(e\room\NPC[0]\obj), 10, 28, 0.12, False)
					e\EventState=e\EventState+FPSfactor
					If e\EventState > 500 Then
						
						If e\EventState > 520 And e\EventState - FPSfactor <= 520 Then BlinkTimer = 0
						If e\EventState < 2000 Then
							If e\SoundCHN = 0 Then
								e\SoundCHN = PlaySound(AlarmSFX(0))
							Else
								If Not ChannelPlaying(e\SoundCHN) Then e\SoundCHN = PlaySound(AlarmSFX(0))
							End If
							If Rand(600) = 1 Then PlaySound(IntroSFX(Rand(7, 9)))
							If Rand(400) = 1 Then PlaySound(IntroSFX(Rand(13, 14)))
						Else
							If Rand(1200) = 1 Then PlaySound(IntroSFX(Rand(7, 9)))
							If Rand(800) = 1 Then PlaySound(IntroSFX(Rand(13, 14)))
						EndIf
						
						If e\EventState > 900 And e\EventState - FPSfactor <= 900 Then e\SoundCHN2 = PlaySound(AlarmSFX(1))
						If e\EventState > 2000 And e\EventState - FPSfactor <= 2000 Then PlaySound(IntroSFX(7))
						If e\EventState > 3500 Then 
							PlaySound(IntroSFX(7))
							If e\room\NPC[0] <> Null Then RemoveNPC(e\room\NPC[0])
							Delete e
						EndIf
					End If
				End If
				
				;Dim IntroSFX%(16)
				;For i = 0 To 3
				;	IntroSFX(i) = LoadSound("SFX\intro\intro" + (i + 1) + ".ogg")
				;Next
				;For i = 4 To 6
				;	IntroSFX(i) = LoadSound("SFX\intro\refuse" + (i - 3) + ".ogg")
				;Next
				;For i = 7 To 9
				;	IntroSFX(i) = LoadSound("SFX\intro\bang" + (i - 6) + ".ogg")
				;Next
				;For i = 10 To 12
				;	IntroSFX(i) = LoadSound("SFX\intro\elec" + (i - 9) + ".ogg")
				;Next
				;IntroSFX(13) = LoadSound("SFX\intro\shoot1.ogg")
				;IntroSFX(14) = LoadSound("SFX\intro\shoot2.ogg")
				;IntroSFX(15) = LoadSound("SFX\intro\metal173.ogg")
				
			Case "173" ;the intro sequence
				If KillTimer >= 0 And e\EventState2 = 0 Then
					
					If e\EventState3>0 Then
						
						If Music(5)=0 Then Music(5) = LoadSound("SFX\Music\Blue Feather.ogg")
						ShouldPlay = 5
						
						If e\EventState3 < 150 Then 
							e\EventState3 = e\EventState3+FPSfactor/30.0
							If e\EventState3 < 30 Then
								If e\EventState3 > 5 Then 
									e\EventState3 = e\EventState3+FPSfactor/3.0
									Msg = "Pick up the paper on the desk and open the inventory by pressing tab"
									MsgTimer=70*7
									e\EventState3=30
								EndIf
							EndIf
							
							If SelectedItem <> Null Then
								e\EventState3 = e\EventState3+FPSfactor/5.0
							EndIf							
							;If e\EventState3 => 150 Then BlinkTimer = -10
						ElseIf e\EventState3 => 150.0 And e\EventState3 < 700
							If e\room\NPC[3] = Null Then
								BlinkTimer = -10
								
								e\room\NPC[3] = CreateNPC(NPCtypeGuard, e\room\x-3072*RoomScale+Rnd(-0.3,0.3), 0.3, e\room\z+Rand(860,896)*RoomScale)
								e\room\NPC[4] = CreateNPC(NPCtypeGuard, e\room\x-2560*RoomScale, 0.3, e\room\z+768*RoomScale)
								e\room\NPC[5] = CreateNPC(NPCtypeGuard, e\room\x-6400*RoomScale, 0.3, e\room\z+768*RoomScale)
								e\room\NPC[6] = CreateNPC(NPCtypeD, e\room\x-3712*RoomScale, -0.3, e\room\z-2208*RoomScale)
								tex = LoadTexture("GFX\npcs\scientist2.jpg")
								EntityTexture e\room\NPC[6]\obj, tex
								
								e\Sound = LoadSound("SFX\intro\guard1.ogg")
								PlaySound2(e\Sound, Camera, e\room\NPC[3]\Collider)
								
								e\room\NPC[3]\State = 7
								e\room\NPC[4]\State = 7
								e\room\NPC[5]\State = 7								
								;PositionEntity e\room\NPC[1], e\room\x-3584*RoomScale, 0.3, e\room\z+608*RoomScale
								;ResetEntity e\room\NPC[1]
								;PositionEntity e\room\NPC[2], e\room\x-409*RoomScale, 0.3, e\room\z+608*RoomScale
								;ResetEntity e\room\NPC[2]
								
								e\room\RoomDoors[6]\locked = False		
								UseDoor(e\room\RoomDoors[6], False)
								e\room\RoomDoors[6]\locked = True									
							EndIf
							
							e\room\NPC[3]\State = 7
							PointEntity e\room\NPC[3]\Collider, Collider
							PointEntity e\room\NPC[4]\Collider, Collider
							PointEntity e\room\NPC[5]\Collider, Collider
							e\EventState3 = Min(e\EventState3+FPSfactor/4,699)
							If Distance(EntityX(Collider),EntityZ(Collider),PlayerRoom\x-3072*RoomScale, PlayerRoom\z+192.0*RoomScale)>1.5 Then
								e\room\NPC[3]\State = 5
								e\room\NPC[3]\EnemyX = EntityX(Collider)
								e\room\NPC[3]\EnemyY = EntityY(Collider)
								e\room\NPC[3]\EnemyZ = EntityZ(Collider)
								
								If e\EventState3 > 250 Then
									If e\room\NPC[3]\SoundChn<>0 Then
										If ChannelPlaying(e\room\NPC[3]\SoundChn) Then StopChannel e\room\NPC[3]\SoundChn
									EndIf
									e\Sound = LoadSound("SFX\intro\guard4.ogg")
									PlaySound2(e\Sound, Camera, e\room\NPC[3]\Collider)
									
									e\room\NPC[3]\PathStatus = FindPath(e\room\NPC[3],PlayerRoom\x-1584*RoomScale, 0.3, PlayerRoom\z-1040*RoomScale)
									;e\room\NPC[3]\State = 3	
									DebugLog e\room\NPC[3]\PathStatus
									
									e\EventState3 = 710
								EndIf
							Else
								If e\EventState3-(FPSfactor/4) < 350 And e\EventState3=>350 Then
									e\Sound = LoadSound("SFX\intro\guard2.ogg")
									e\room\NPC[3]\SoundChn = PlaySound2(e\Sound, Camera, e\room\NPC[3]\Collider)	
								ElseIf e\EventState3-(FPSfactor/4) < 550 And e\EventState3=>550 
									e\Sound = LoadSound("SFX\intro\guard3.ogg")
									e\room\NPC[3]\SoundChn = PlaySound2(e\Sound, Camera, e\room\NPC[3]\Collider)
								ElseIf e\EventState3>630
									PositionEntity Collider, EntityX(Collider), EntityY(Collider), Min(EntityZ(Collider), EntityZ(e\room\obj,True)+490*RoomScale)
									If e\room\RoomDoors[6]\open = True Then 
										e\room\RoomDoors[6]\locked = False		
										UseDoor(e\room\RoomDoors[6],False)
										e\room\RoomDoors[6]\locked = True
										
										em.Emitters = CreateEmitter(PlayerRoom\x - 2976.0 * RoomScale, 373.0 * RoomScale, PlayerRoom\z + 204.0 * RoomScale, 0)
										TurnEntity(em\Obj, 90, 0, 0, True)
										em\RandAngle = 7
										em\Speed = 0.03
										em\SizeChange = 0.003
										em\Room = PlayerRoom
										
										em.Emitters = CreateEmitter(PlayerRoom\x - 3168.0 * RoomScale, 373.0 * RoomScale, PlayerRoom\z + 204.0 * RoomScale, 0)
										TurnEntity(em\Obj, 90, 0, 0, True)
										em\RandAngle = 7
										em\Speed = 0.03
										em\SizeChange = 0.003
										em\Room = PlayerRoom
									EndIf
									
									EyeIrritation=Max(EyeIrritation+FPSfactor * 4, 1.0)			
								ElseIf e\EventState3>670
									
								EndIf
								
							EndIf
						ElseIf e\EventState3 < 800
							e\EventState3 = e\EventState3+FPSfactor/4.0
							
							DebugLog "***********************************"
							DebugLog e\room\NPC[3]\State
							
							e\room\NPC[3]\State = 5
							e\room\NPC[3]\EnemyX = EntityX(Collider)
							e\room\NPC[3]\EnemyY = EntityY(Collider)
							e\room\NPC[3]\EnemyZ = EntityZ(Collider)
						ElseIf e\EventState3 < 900
							
							;the scientist
							If e\room\NPC[6]<>Null Then 
								If e\room\NPC[6]\State = 0 Then 
									If Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\obj,True)-3328*RoomScale, EntityZ(e\room\obj,True)-1232*RoomScale)<5.0 Then
										e\room\NPC[6]\State = 1
									EndIf
								Else
									If EntityZ(e\room\NPC[6]\Collider)>EntityZ(e\room\obj,True)-64.0*RoomScale Then
										RotateEntity e\room\NPC[6]\Collider, 0, CurveAngle(90,EntityYaw(e\room\NPC[6]\Collider),15.0),0
										If e\room\RoomDoors[7]\open Then UseDoor(e\room\RoomDoors[7],False)
										If e\room\RoomDoors[7]\openstate < 1.0 Then e\room\NPC[6]\State = 0
									EndIf
								EndIf
							EndIf
								
							dist = Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\NPC[3]\Collider), EntityZ(e\room\NPC[3]\Collider))
							
							If dist < 3.0 Then
								e\room\NPC[3]\State3 = Min(Max(e\room\NPC[3]\State3-FPSfactor,0),50)
							Else
								e\room\NPC[3]\State3 = Max(e\room\NPC[3]\State3+FPSfactor,50)
								If e\room\NPC[3]\State3 => 70*8 And e\room\NPC[3]\State3-FPSfactor < 70*8 And e\room\NPC[3]\State=7 Then
									If e\room\NPC[3]\State2 < 2 Then
										e\Sound = LoadSound("SFX\intro\guard6.ogg")
										PlaySound2(e\Sound, Camera, e\room\NPC[3]\Collider)
										e\room\NPC[3]\State3=50
										e\room\NPC[3]\State2=3
									ElseIf e\room\NPC[3]\State2=3
										e\Sound = LoadSound("SFX\intro\guard7.ogg")
										PlaySound2(e\Sound, Camera, e\room\NPC[3]\Collider)
										e\room\NPC[3]\State3=50
										e\room\NPC[3]\State2=4
									ElseIf e\room\NPC[3]\State2=4
										e\Sound = LoadSound("SFX\intro\guard8.ogg")
										PlaySound2(e\Sound, Camera, e\room\NPC[3]\Collider)
										e\room\NPC[3]\State3 = 50+70*2.5
										e\room\NPC[3]\State2=5
									ElseIf e\room\NPC[3]\State2=5
										e\room\NPC[3]\State = 1
										e\room\NPC[4]\State = 1
										e\room\NPC[5]\State = 1
									EndIf									
								EndIf
							EndIf
							
							If e\room\NPC[3]\Sound <> 0 Then
								If ChannelPlaying(e\room\NPC[3]\SoundChn)=False Then 
									e\room\NPC[3]\Sound=0
								Else
									e\room\NPC[3]\SoundChn=LoopSound2(e\room\NPC[3]\Sound, e\room\NPC[3]\SoundChn, Camera, e\room\NPC[3]\Collider)
									
								EndIf
							EndIf
							
							DebugLog e\room\NPC[3]\State3
							
							;state3 	= 0-50 silloin kun pelaaja on l‰hell‰
							;			= 50-x silloin kun pelaaja kaukana
							
							If e\room\NPC[3]\State <> 1 Then 
								e\room\NPC[4]\State = 5
								e\room\NPC[4]\EnemyX = EntityX(Collider)
								e\room\NPC[4]\EnemyY = EntityY(Collider)
								e\room\NPC[4]\EnemyZ = EntityZ(Collider)
								
								PointEntity e\room\NPC[5]\Collider, Collider
								
								If dist < Min(Max(4.0-e\room\NPC[3]\State3*0.05, 1.5),4.0) Then
									If e\room\NPC[3]\PathStatus <> 1 Then
										e\room\NPC[3]\State = 7
										PointEntity e\room\NPC[3]\obj, Collider
										RotateEntity e\room\NPC[3]\Collider,0,CurveValue(EntityYaw(e\room\NPC[3]\obj),EntityYaw(e\room\NPC[3]\Collider),20.0),0,True
										
										If e\room\NPC[3]\PathStatus = 2 Then
											e\room\NPC[3]\PathStatus = FindPath(e\room\NPC[3],PlayerRoom\x-1584*RoomScale, 0.3, PlayerRoom\z-1040*RoomScale)
											e\room\NPC[3]\State = 3
										EndIf
									Else
										e\room\NPC[3]\State = 3
									EndIf
								Else
									DebugLog "state = 7"
									e\room\NPC[3]\State = 7
									PointEntity e\room\NPC[3]\obj, Collider
									RotateEntity e\room\NPC[3]\Collider,0,CurveValue(EntityYaw(e\room\NPC[3]\obj),EntityYaw(e\room\NPC[3]\Collider),20.0),0,True		
									
									If dist > 5.5 Then
										e\room\NPC[3]\PathStatus = 2
										If e\room\NPC[3]\State2=0 Then
											e\Sound = LoadSound("SFX\intro\guard5.ogg")
											PlaySound2(e\Sound, Camera, e\room\NPC[3]\Collider)
											e\room\NPC[3]\State2=1
										EndIf
										
										e\room\NPC[3]\State = 5
										e\room\NPC[3]\EnemyX = EntityX(Collider)
										e\room\NPC[3]\EnemyY = EntityY(Collider)
										e\room\NPC[3]\EnemyZ = EntityZ(Collider)
										;e\room\NPC[3]\PathStatus = FindPath(e\room\NPC[3],EntityY(Collider), 0.3, EntityZ(Collider))
									EndIf
								EndIf	
									
							EndIf
							
							If Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\RoomDoors[2]\frameobj,True), EntityZ(e\room\RoomDoors[2]\frameobj,True)) < 1.5 Then
								e\Sound = LoadSound("SFX\intro\guard10.ogg")
								PlaySound2(e\Sound, Camera, e\room\NPC[3]\Collider)
								
								PositionEntity e\room\NPC[6]\Collider, EntityX(e\room\obj,True)-1190*RoomScale, 450*RoomScale, EntityZ(e\room\obj, True)+456*RoomScale, True
								ResetEntity e\room\NPC[6]\Collider
								PointEntity e\room\NPC[6]\Collider, e\room\obj
								e\room\NPC[6]\CurrSpeed = 0
								e\room\NPC[6]\State = 0
								
								e\EventState3 = 910
								
								e\room\RoomDoors[3]\locked = False
								UseDoor(e\room\RoomDoors[3],False)
								e\room\RoomDoors[3]\locked = True
								
								e\room\RoomDoors[2]\locked = False
								UseDoor(e\room\RoomDoors[2],False)
								e\room\RoomDoors[2]\locked = True
							EndIf
						Else
							e\room\NPC[3]\State = 7
							PointEntity e\room\NPC[3]\obj, Collider
							RotateEntity e\room\NPC[3]\Collider,0,CurveValue(EntityYaw(e\room\NPC[3]\obj),EntityYaw(e\room\NPC[3]\Collider),20.0),0,True							
							e\room\NPC[4]\State = 7
							PointEntity e\room\NPC[4]\obj, Collider
							RotateEntity e\room\NPC[4]\Collider,0,CurveValue(EntityYaw(e\room\NPC[3]\obj),EntityYaw(e\room\NPC[3]\Collider),20.0),0,True	
							
							If Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\obj), EntityZ(e\room\obj)) < 2.7 Then
								e\room\RoomDoors[2]\locked = False
								UseDoor(e\room\RoomDoors[2],False)
								e\room\RoomDoors[2]\locked = True			
								e\EventState3 = 0
								e\room\NPC[3]\State = 0
								e\room\NPC[4]\State = 0
								e\room\NPC[5]\State = 0
								
								UseDoor(e\room\RoomDoors[1],False)
							EndIf							
						EndIf
					Else
						
						;[Block]
						If e\EventState = 0 Then
							If PlayerRoom = e\room Then
								For i = 0 To 3
									IntroSFX(i) = LoadSound("SFX\intro\intro" + (i + 1) + ".ogg")
								Next
								For i = 4 To 6
									IntroSFX(i) = LoadSound("SFX\intro\refuse" + (i - 3) + ".ogg")
								Next
								IntroSFX(16) = LoadSound("SFX\intro\horror.ogg")
								
								Curr173\Idle = True
								
								e\room\NPC[0] = CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[0], True), EntityY(e\room\Objects[0], True), EntityZ(e\room\Objects[0], True))
								e\room\NPC[0]\Angle = 180
								
								e\room\NPC[1] = CreateNPC(NPCtypeD, EntityX(e\room\Objects[1], True), 0.5, EntityZ(e\room\Objects[1], True))
								PointEntity(e\room\NPC[1]\Collider, e\room\Objects[5])
								e\room\NPC[2] = CreateNPC(NPCtypeD, EntityX(e\room\Objects[2], True), 0.5, EntityZ(e\room\Objects[2], True))
								PointEntity(e\room\NPC[2]\Collider, e\room\Objects[5])
								
								tex = LoadTexture("GFX\npcs\classd2.jpg")
								EntityTexture e\room\NPC[2]\obj, tex
								
								PositionEntity(Curr173\Collider, EntityX(e\room\Objects[5], True), 0.5, EntityZ(e\room\Objects[5], True))
								ResetEntity(Curr173\Collider)
								
								PositionEntity Collider, PlayerRoom\x-3072*RoomScale, 0.3, PlayerRoom\z+192.0*RoomScale
								ResetEntity Collider
								
								;e\room\roomdoors(1).locked = False
								;e\room\roomdoors(1).Use()
								
								e\EventState = 1
								e\EventState3 = 1
							EndIf
						ElseIf e\EventState < 10000
							If e\room\NPC[6]\SoundChn<>0 Then 
								If ChannelPlaying (e\room\NPC[6]\SoundChn) Then
									e\room\NPC[6]\State = 2
										;PointEntity e\room\NPC[6]\Collider, e\room\obj
									If AnimTime(e\room\NPC[6]\obj)=>325 Then
										Animate2(e\room\NPC[6]\obj, AnimTime(e\room\NPC[6]\obj),326,328, 0.02*FPSfactor, False)
									Else
										Animate2(e\room\NPC[6]\obj, AnimTime(e\room\NPC[6]\obj),320,328, 0.05*FPSfactor, False)
									EndIf
								Else
									Animate2(e\room\NPC[6]\obj,AnimTime(e\room\NPC[6]\obj), 328,320,-0.02*FPSfactor, False)
								EndIf
							EndIf							
							
							e\EventState = Min(e\EventState + (FPSfactor / 3), 5000)
							If e\EventState >= 130 And e\EventState - (FPSfactor/3) < 130 Then
								e\room\NPC[6]\SoundChn = PlaySound(IntroSFX(0))
							ElseIf e\EventState > 230
								temp = True
								For i = 1 To 2
									If Distance(EntityX(e\room\NPC[i]\Collider), EntityZ(e\room\NPC[i]\Collider), EntityX(e\room\Objects[i + 2], True), EntityZ(e\room\Objects[i + 2], True)) > 0.3 Then
										
										
										PointEntity(e\room\NPC[i]\obj, e\room\Objects[i + 2])
										RotateEntity(e\room\NPC[i]\Collider, 0, CurveValue(EntityYaw(e\room\NPC[i]\obj),EntityYaw(e\room\NPC[i]\Collider),15.0),0)
										;moveentity(e\room\npc(i).Collider, 0, 0, 0.015 * FPSfactor)
										If e\EventState > (200 + i * 30) Then e\room\NPC[i]\State = 1
										temp = False
									Else
										e\room\NPC[i]\State = 0
										
										PointEntity(e\room\NPC[i]\obj, e\room\Objects[5])
										RotateEntity(e\room\NPC[i]\Collider, 0, CurveValue(EntityYaw(e\room\NPC[i]\obj),EntityYaw(e\room\NPC[i]\Collider),15.0), 0)
										
										
										;PointEntity(e\room\NPC[i]\Collider, e\room\Objects[5])
									EndIf
								Next
								
								If EntityX(Collider) < (EntityX(e\room\obj)) + 408.0 * RoomScale Then
									If e\EventState => 450 And e\EventState - (FPSfactor/3) < 450 Then ;"mene huoneeseen"
										e\room\NPC[6]\SoundChn = PlaySound(IntroSFX(4))
									ElseIf e\EventState => 650 And e\EventState - (FPSfactor/3) < 650 ;"viimeinen varoitus, 5 sek aikaa"
										e\room\NPC[6]\SoundChn = PlaySound(IntroSFX(5))
									ElseIf e\EventState => 850 And e\EventState - (FPSfactor/3) < 850 ;"fire at will"
										;UseDoor(e\room\RoomDoors[1])
										e\room\RoomDoors[1]\open = False
										e\room\NPC[6]\SoundChn = PlaySound(IntroSFX(6))
									ElseIf e\EventState > 1000
										e\room\NPC[0]\State = 1
										e\EventState2 = 1
										Exit
									EndIf
									
									If e\EventState > 850 Then
										PositionEntity(Collider, Min(EntityX(Collider), EntityX(e\room\obj) + 352.0 * RoomScale), EntityY(Collider), EntityZ(Collider))
									End If
								ElseIf temp = True ;pelaaja ja molemmat npc:t huoneessa
									e\EventState = 10000
									UseDoor(e\room\RoomDoors[1],False)
								End If
							End If
							
							e\room\NPC[6]\State = 7
							PointEntity e\room\NPC[6]\obj, Collider
							RotateEntity e\room\NPC[6]\Collider,0,CurveValue(EntityYaw(e\room\NPC[6]\obj),EntityYaw(e\room\NPC[6]\Collider),20.0),0,True	
							
							PositionEntity(Curr173\Collider, EntityX(e\room\Objects[5], True), EntityY(Curr173\Collider), EntityZ(e\room\Objects[5], True))
							RotateEntity(Curr173\Collider, 0, 0, 0, True)
							ResetEntity(Curr173\Collider)
						ElseIf e\EventState < 14000 ; pelaaja sis‰ll‰ huoneessa
							e\EventState = Min(e\EventState + FPSfactor, 13000)
							
							If e\EventState < 10300 Then
								PositionEntity(Collider, Max(EntityX(Collider), EntityX(e\room\obj) + 352.0 * RoomScale), EntityY(Collider), EntityZ(Collider))
							End If
							
							e\room\NPC[6]\State = 2
							PointEntity e\room\NPC[6]\obj, Curr173\Collider
							RotateEntity e\room\NPC[6]\Collider,0,CurveValue(EntityYaw(e\room\NPC[6]\obj),EntityYaw(e\room\NPC[6]\Collider),50.0),0,True	
							
							If e\EventState => 10300 And e\EventState - FPSfactor < 10300 Then ;"l‰hestyk‰‰ 173:a"
								e\SoundCHN = PlaySound(IntroSFX(1))
								PositionEntity(Collider, Max(EntityX(Collider), EntityX(e\room\obj) + 352.0 * RoomScale), EntityY(Collider), EntityZ(Collider))
							ElseIf e\EventState => 10440 And e\EventState - FPSfactor < 10440 ;ovi aukee itsest‰‰n
								UseDoor(e\room\RoomDoors[1],False)
								e\SoundCHN = PlaySound(IntroSFX(7)) ;pamahdus
							ElseIf e\EventState => 10740 And e\EventState - FPSfactor < 10740 ;"nyt on jotain ongelmia"
								e\SoundCHN = PlaySound(IntroSFX(2))
							ElseIf e\EventState => 11490 And e\EventState - FPSfactor < 11490;"s‰hkˆ‰‰ni"
								e\SoundCHN = PlaySound(IntroSFX(10))
							ElseIf e\EventState => 11561 And e\EventState - FPSfactor < 11561 ;valot sammuu jne
								e\EventState = 14000
								PlaySound IntroSFX(16)
								TempSound = LoadSound("SFX\intro\wtf.ogg")
								PlaySound2 (TempSound, Camera, e\room\NPC[1]\Collider, 8.0)
							End If
							
							If e\EventState > 10300 Then 
								If AnimTime(e\room\NPC[6]\obj)=>325 Then
									Animate2(e\room\NPC[6]\obj, AnimTime(e\room\NPC[6]\obj),326,328, 0.02*FPSfactor, False)
								Else
									Animate2(e\room\NPC[6]\obj, AnimTime(e\room\NPC[6]\obj),320,328, 0.05*FPSfactor, False)
								EndIf
							EndIf
									
							PositionEntity(Curr173\Collider, EntityX(e\room\Objects[5], True),EntityY(Curr173\Collider), EntityZ(e\room\Objects[5], True))
							RotateEntity(Curr173\Collider, 0, 0, 0, True)
							ResetEntity(Curr173\Collider)
						ElseIf e\EventState < 20000
							pvt% = CreatePivot()
							PositionEntity pvt, EntityX(Camera), EntityY(Curr173\Collider,True)-0.05, EntityZ(Camera)
							PointEntity(pvt, Curr173\Collider)
							RotateEntity(Collider, EntityPitch(Collider), CurveAngle(EntityYaw(pvt), EntityYaw(Collider), 40), 0)
							
							TurnEntity(pvt, 90, 0, 0)
							user_camera_pitch = CurveAngle(EntityPitch(pvt), user_camera_pitch + 90.0, 40)
							user_camera_pitch=user_camera_pitch-90
							FreeEntity pvt
							
							e\room\NPC[6]\State = 2
							PointEntity e\room\NPC[6]\obj, Curr173\Collider
							RotateEntity e\room\NPC[6]\Collider,0,CurveValue(EntityYaw(e\room\NPC[6]\obj),EntityYaw(e\room\NPC[6]\Collider),20.0),0,True	
							Animate2(e\room\NPC[6]\obj, AnimTime(e\room\NPC[6]\obj),357,381, 0.05*FPSfactor)
							
							e\EventState = Min(e\EventState + FPSfactor, 19000)
							If e\EventState < 14100 Then ;valot sammuu ja 173 tappaa ensimm‰isen class d:n
								
								;14000-14030
								If e\EventState < 14060 Then
									BlinkTimer = Max((14000-e\EventState)/2-Rnd(0,1.0),-10);Max(Min(Sin((e\EventState-14000)*9+90)*10-5,10),-10)
									;0-60,   90-640
									If BlinkTimer = -10 Then
										PointEntity Curr173\Collider, e\room\NPC[1]\obj
										RotateEntity(Curr173\Collider, 0, EntityYaw(Curr173\Collider),0)
										MoveEntity Curr173\Collider, 0,0,Curr173\Speed*0.6*FPSfactor
										
										Curr173\SoundChn = LoopSound2(StoneDragSFX, Curr173\SoundChn, Camera, Curr173\Collider, 10.0, Curr173\State)
										
										Curr173\State = CurveValue(1.0, Curr173\State, 3)
										
									Else
										Curr173\State = Max(0, Curr173\State - FPSfactor / 20)
									EndIf
								ElseIf e\EventState < 14065
									BlinkTimer = -10
									If e\room\NPC[1]\State = 0 Then PlaySound2(DamageSFX(Rand(0, 2)),Camera,Curr173\Collider)
									
									Animate2(e\room\NPC[0]\obj, AnimTime(e\room\NPC[0]\obj), 110, 120, 0.1, False)
									e\room\NPC[0]\State=8
									SetAnimTime e\room\NPC[1]\obj, 19
									e\room\NPC[1]\State = 2
									PositionEntity(Curr173\Collider, EntityX(e\room\NPC[1]\obj), EntityY(Curr173\Collider), EntityZ(e\room\NPC[1]\obj))
									ResetEntity(Curr173\Collider)
									PointEntity(Curr173\Collider, e\room\NPC[2]\Collider)
									
									e\room\NPC[2]\State = 3
									RotateEntity e\room\NPC[2]\Collider, 0, EntityYaw(e\room\NPC[2]\Collider), 0
									Animate2(e\room\NPC[2]\obj, AnimTime(e\room\NPC[2]\obj),406,382,-0.01*15)
									MoveEntity e\room\NPC[2]\Collider, 0,0,-0.01*FPSfactor
								Else
									If e\room\NPC[2]\Sound=0 Then 
										e\room\NPC[2]\Sound = LoadSound("SFX\intro\gasp.ogg")
										PlaySound2 (e\room\NPC[2]\Sound, Camera, e\room\NPC[2]\Collider, 8.0)	
									EndIf									
								EndIf
								;If e\EventState > 14032 And e\EventState - FPSfactor < 14032 Then PlaySound(HorrorSFX(2))
								If e\EventState > 14080 And e\EventState - FPSfactor < 14080 Then PlaySound(IntroSFX(12))
								CameraShake = 3
							ElseIf e\EventState < 14200 ;tappaa toisen class d:n
								Animate2(e\room\NPC[0]\obj, AnimTime(e\room\NPC[0]\obj), 110, 120, 0.2, False)
								e\room\NPC[0]\State=8
								If e\EventState > 14105 Then
									If AnimTime(e\room\NPC[2]\obj)<>19 Then PlaySound2 (DamageSFX(1), Camera, e\room\NPC[2]\Collider, 8.0)
									SetAnimTime e\room\NPC[2]\obj, 19
									e\room\NPC[2]\State = 2
									PositionEntity(Curr173\Collider, EntityX(e\room\NPC[2]\obj), EntityY(Curr173\Collider), EntityZ(e\room\NPC[2]\obj))
									ResetEntity(Curr173\Collider)
									PointEntity(Curr173\Collider, Collider)
								EndIf
								If e\EventState < 14130 Then BlinkTimer = -10 : LightBlink = 1.0 Else Curr173\Idle = False
								If e\EventState > 14100 And e\EventState - FPSfactor < 14100 Then PlaySound(IntroSFX(8))
								If e\EventState < 14150 Then CameraShake = 5
							ElseIf e\EventState > 14300
								If e\EventState > 14600 And e\EventState < 14700 Then BlinkTimer = -10 : LightBlink = 1.0
								If EntityX(Collider) < (EntityX(e\room\obj)) + 448.0 * RoomScale Then e\EventState = 20000
							End If
						ElseIf e\EventState < 30000
							e\EventState = Min(e\EventState + FPSfactor, 30000)
							If e\EventState < 20100 Then
								CameraShake = 2
							Else
								If e\EventState < 20200 Then ;valot sammuu ja 173 menee sotilaan viereen
									If e\EventState > 20105 And e\EventState - FPSfactor < 20105 Then 
										Delete e\room\NPC[i]
										PlaySound(IntroSFX(9))
										PositionEntity(e\room\NPC[0]\Collider, EntityX(e\room\obj) - 160.0 * RoomScale, EntityY(e\room\NPC[0]\Collider) + 0.1, EntityZ(e\room\obj) + 1280.0 * RoomScale)
										ResetEntity(e\room\NPC[0]\Collider)										
									EndIf
									If e\EventState > 20105 Then
										Curr173\Idle = True 
										PointEntity(e\room\NPC[0]\Collider, Curr173\obj)
										PositionEntity(Curr173\Collider, EntityX(e\room\obj) - 608.0 * RoomScale, EntityY(e\room\obj) + 480.0 * RoomScale, EntityZ(e\room\obj) + 1312.0 * RoomScale)
										ResetEntity(Curr173\Collider)
										PointEntity(Curr173\Collider, e\room\NPC[0]\Collider)
									EndIf
									
									BlinkTimer = -10 : LightBlink = 1.0
									CameraShake = 3
								ElseIf e\EventState < 20300 ;valot syttyy, sotilas alkaa ampua 173:a
									PointEntity(e\room\NPC[0]\Collider, Curr173\Collider)
									MoveEntity(e\room\NPC[0]\Collider, 0, 0, -0.002)
									e\room\NPC[0]\State = 2
									If e\EventState > 20260 And e\EventState - FPSfactor < 20260 Then PlaySound(IntroSFX(12))
								Else ;valot sammuu uudestaan, sotilas kuolee
									
									If e\EventState - FPSfactor < 20300 Then
										BlinkTimer = -10 : LightBlink = 1.0
										CameraShake = 3
										PlaySound(IntroSFX(11))
										PlaySound2 (DamageSFX(1), Camera, e\room\NPC[0]\Collider, 8.0)
										
										Curr173\Idle = False
										
										e\SoundCHN = PlaySound(IntroSFX(15))
										
										PositionEntity(Curr173\Collider, EntityX(PlayerRoom\obj) - 400.0 * RoomScale, 100.0, EntityZ(PlayerRoom\obj) + 1072.0 * RoomScale)
										ResetEntity(Curr173\Collider)
										
										For r.Rooms = Each Rooms
											If r\RoomTemplate\Name = "start" Then
												
												PlayerRoom = r
												
												Local x# = EntityX(r\obj, True), z# = EntityZ(r\obj, True)
												
												PositionEntity(Collider, x + (EntityX(Collider) - EntityX(e\room\obj)), 1.0, z + (EntityZ(Collider) - EntityZ(e\room\obj)))
												ResetEntity(Collider)
												;PositionEntity(Curr173\Collider, x + (EntityX(Curr173\Collider) - EntityX(e\room\obj)), EntityY(Curr173\Collider), z + (EntityZ(Curr173\Collider) - EntityZ(e\room\obj)))
												;ResetEntity(Curr173\Collider)
												For i = 0 To 2
													PositionEntity(e\room\NPC[i]\Collider, x + (EntityX(e\room\NPC[i]\Collider) - EntityX(e\room\obj)), EntityY(e\room\NPC[i]\Collider)+0.05, z + (EntityZ(e\room\NPC[i]\Collider) - EntityZ(e\room\obj)))
													ResetEntity(e\room\NPC[i]\Collider)
												Next
												
												FreeSound Music(5)
												ShouldPlay = 0
												
												r\NPC[0]=e\room\NPC[0]
												r\NPC[0]\State=8
												
												For do.doors = Each Doors
													If do\room = e\room Then
														Delete do
													EndIf
												Next
												
												For w.waypoints = Each WayPoints
													If w\room = e\room Then 
														FreeEntity w\obj
														Delete w
													EndIf
												Next
												
												For i = 3 To 5
													Delete e\room\NPC[i]
												Next
												
												FreeEntity e\room\obj
												Delete e\room
												
												ShowEntity Fog
												AmbientLight Brightness, Brightness, Brightness
												CameraFogRange(Camera, CameraFogNear, CameraFogFar)
												CameraFogMode(Camera, 1)
												
												e\EventState2 = 1
												
												Exit
											EndIf
										Next
									EndIf
									
								EndIf
								
							EndIf
						EndIf
						
						;[End block]
						
					EndIf
					
				Else
					If KillTimer<0 Then
						If e\room\NPC[3]\State = 1 Then 
							e\Sound = LoadSound("SFX\intro\guard9.ogg")
							PlaySound e\Sound
						EndIf
					EndIf
					
					For i = 0 To 6
						If IntroSFX(i)<>0 Then FreeSound IntroSFX(i) : IntroSFX(i)=0
					Next
					FreeSound IntroSFX(16) : IntroSFX(16)=0
					
					e\EventState2 = 1
				EndIf
				
				If PlayerRoom = e\room Then
					;ShouldPlay = 66
					CameraFogMode(Camera, 0)
					AmbientLight (140, 140, 140)
					HideEntity(Fog)
					
					LightVolume = 4.0
					TempLightVolume = 4.0
				Else
					Delete e			
				EndIf	
			Case "gatea"
				If PlayerRoom = e\room Then 
					If e\EventState = 0 Then
						DrawLoading(0)
						e\room\Objects[0] = LoadMesh("GFX\MAP\gateatunnel.b3d")
						PositionEntity e\room\Objects[0], EntityX(e\room\obj,True),EntityY(e\room\obj,True),EntityZ(e\room\obj,True)
						ScaleEntity (e\room\Objects[0],RoomScale,RoomScale,RoomScale)
						EntityType e\room\Objects[0], HIT_MAP
						EntityPickMode e\room\Objects[0], 3
						EntityParent(e\room\Objects[0],e\room\obj)
						
						DrawLoading(30)
						
						For i = 0 To 19
							If e\room\LightSprites[i]<>0 Then 
								EntityFX e\room\LightSprites[i], 1+8
							EndIf
						Next
						
						HideDistance = 35.0
						
						For i = 2 To 4
							e\room\NPC[i] = CreateNPC(NPCtypeApache, e\room\x, 100.0, e\room\z)
							e\room\NPC[i]\State = 1
						Next
						
						Music(5) = LoadSound("SFX\Music\Satiate.ogg")
							
						CreateConsoleMsg("WARNING! Teleporting away from this area may cause bugs or crashing.")
						
						PositionEntity Sky1, EntityX(e\room\obj,True),EntityY(Sky1,True),EntityZ(e\room\obj,True), True
						PositionEntity Sky2, EntityX(e\room\obj,True),EntityY(Sky2,True),EntityZ(e\room\obj,True), True						
						
						TranslateEntity(e\room\obj, 0,12000.0*RoomScale,0)
						TranslateEntity(Collider, 0,12000.0*RoomScale,0)
						
						DrawLoading(60)
						
						For n.NPCs = Each NPCs
							If n\NPCtype = NPCtypeMTF Then Delete n
						Next
						
						For i = 0 To 1
							e\room\NPC[i] = CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[i+5],True),EntityY(e\room\Objects[i+5],True),EntityZ(e\room\Objects[i+5],True))
							e\room\NPC[i]\State = 0
							PointEntity e\room\NPC[i]\Collider, e\room\Objects[3]
						Next
						
						For i = 7 To 8
							e\room\NPC[i] = CreateNPC(NPCtypeMTF, EntityX(e\room\Objects[i],True)+0.8,EntityY(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True)+0.8)
							e\room\NPC[i]\State = 5
							e\room\NPC[i]\PrevState = 1
							PointEntity e\room\NPC[i]\Collider, e\room\Objects[3]
						Next	
						
						For i = 5 To 6
							e\room\NPC[i] = CreateNPC(NPCtypeMTF, EntityX(e\room\Objects[i+2],True),EntityY(e\room\Objects[i+2],True),EntityZ(e\room\Objects[i+2],True))
							e\room\NPC[i]\State = 5
							e\room\NPC[i]\PrevState = 1
							PointEntity e\room\NPC[i]\Collider, e\room\Objects[3]
						Next		
						
						If Contained106 Then
							e\room\RoomDoors[2]\locked = True
							
							For i = 5 To 6
								PositionEntity e\room\NPC[i]\Collider, EntityX(e\room\Objects[15],True)+(i-6)*0.2,EntityY(e\room\Objects[15],True),EntityZ(e\room\Objects[15],True)+(i-6)*0.2, True
								ResetEntity e\room\NPC[i]\Collider
							Next
							
							For i = 5 To 8
								e\room\NPC[i]\State = 2
							Next
						EndIf
						
						xtemp#=EntityX(e\room\Objects[9],True)
						ztemp#=EntityZ(e\room\Objects[9],True)
						FreeEntity e\room\Objects[9]
						
						e\room\Objects[9] = LoadMesh("GFX\map\lightgunbase.b3d")
						ScaleEntity e\room\Objects[9], RoomScale,RoomScale,RoomScale
						PositionEntity(e\room\Objects[9], xtemp, (992.0+12000.0)*RoomScale, ztemp)
						e\room\Objects[10] = LoadMesh("GFX\map\lightgun.b3d")
						ScaleEntity e\room\Objects[10], RoomScale,RoomScale,RoomScale
						PositionEntity(e\room\Objects[10], xtemp, (992.0+12000.0+288.0)*RoomScale, ztemp-176.0*RoomScale,True)
						EntityParent e\room\Objects[10],e\room\Objects[9]
						RotateEntity e\room\Objects[9], 0, 48, 0
						RotateEntity e\room\Objects[10], 40, 0, 0
						
						For temp = 0 To 20
							For i = 0 To 1
								TranslateEntity e\room\NPC[i]\Collider, 0, -0.04, 0
							Next							
							For i = 5 To 8
								TranslateEntity e\room\NPC[i]\Collider, 0, -0.04, 0
							Next
						Next
						
						ResetEntity Collider
						e\EventState = 1.0
						TempSound = LoadSound("SFX\106escape2.ogg")
						DrawLoading(100)
						
						PlaySound TempSound 
					Else
						
						ShouldPlay = 5
						
						e\EventState = e\EventState+FPSfactor
						HideEntity Fog
						CameraFogRange Camera, 5,30
						CameraFogColor (Camera,200,200,200)
						CameraClsColor (Camera,200,200,200)					
						CameraRange(Camera, 0.05, 30)
						
						For i = 2 To 4
							If e\room\NPC[i]<>Null Then 
								If e\room\NPC[i]\State < 2 Then 
									PositionEntity(e\room\NPC[i]\Collider, EntityX(e\room\Objects[3],True)+Cos(e\EventState/10+(120*i))*6000.0*RoomScale,15000*RoomScale,EntityZ(e\room\Objects[3],True)+Sin(e\EventState/10+(120*i))*6000.0*RoomScale)
									RotateEntity e\room\NPC[i]\Collider,7.0,(e\EventState/10+(120*i)),20.0
								EndIf
							EndIf
						Next
						
						PositionTexture(e\room\Objects[1],e\EventState/1500.0, 0)
						PositionTexture(e\room\Objects[2],e\EventState/2500.0, 0)
						
						If e\EventState=>350 Then
							If Contained106=False Then
								If e\EventState-FPSfactor < 350
									Curr106\State = -0.1
									Curr106\Idle = True
									SetAnimTime Curr106\obj, 110.0
									PositionEntity (Curr106\Collider, EntityX(e\room\Objects[3],True),EntityY(Collider)-14.0,EntityZ(e\room\Objects[3],True),True)
									PositionEntity (Curr106\obj, EntityX(e\room\Objects[3],True),EntityY(Collider)-14.0,EntityZ(e\room\Objects[3],True),True)
									de.Decals = CreateDecal(0, EntityX(e\room\Objects[3],True),EntityY(e\room\Objects[3],True)+0.01,EntityZ(e\room\Objects[3],True), 90, Rand(360), 0)
									de\Size = 0.05 : de\SizeChange = 0.001 : EntityAlpha(de\obj, 0.8) : UpdateDecals() 
									PlaySound (HorrorSFX(5))
									PlaySound DecaySFX(0)
								ElseIf Curr106\State < 0
									HideEntity Curr106\obj2
									Curr106\PathTimer = 70*100
									
									If Curr106\State3 = 0 Then
										If Curr106\PathStatus <> 1 Then
											If Curr106\State =< -10 Then 
												dist# = EntityY(Curr106\Collider)
												PositionEntity Curr106\Collider,EntityX(Curr106\Collider),EntityY(e\room\Objects[3],True)+0.3,EntityZ(Curr106\Collider),True
												Curr106\PathStatus = FindPath(Curr106, EntityX(e\room\Objects[4],True),EntityY(e\room\Objects[4],True),EntityZ(e\room\Objects[4],True))
												PositionEntity Curr106\Collider,EntityX(Curr106\Collider),dist,EntityZ(Curr106\Collider),True
												Curr106\PathLocation = 1
												Curr106\Idle = False
											Else	
												PositionEntity (Curr106\Collider, EntityX(e\room\Objects[3],True),EntityY(e\room\Objects[3],True),EntityZ(e\room\Objects[3],True),True)
												Curr106\Idle = True
												Animate2(Curr106\obj, AnimTime(Curr106\obj), 110, 259, 0.15, False)
												If AnimTime(Curr106\obj)=>259 Then Curr106\Idle = False													
												
											EndIf
										Else
											For i = 2 To 4 ;helikopterit hyˆkk‰‰ 106:n kimppuun
												e\room\NPC[i]\State = 3 
												e\room\NPC[i]\EnemyX = EntityX(Curr106\obj,True)
												e\room\NPC[i]\EnemyY = EntityY(Curr106\obj,True)+5.0
												e\room\NPC[i]\EnemyZ = EntityZ(Curr106\obj,True)
											Next
											
											For i = 5 To 8
												e\room\NPC[i]\State = 5
												e\room\NPC[i]\EnemyX = EntityX(Curr106\obj,True)
												e\room\NPC[i]\EnemyY = EntityY(Curr106\obj,True)+0.4
												e\room\NPC[i]\EnemyZ = EntityZ(Curr106\obj,True)											
											Next
											
											pvt=CreatePivot()
											PositionEntity pvt, EntityX(e\room\Objects[10],True),EntityY(e\room\Objects[10],True),EntityZ(e\room\Objects[10],True)
											PointEntity pvt, Curr106\Collider
											RotateEntity(e\room\Objects[9],0,CurveAngle(EntityYaw(pvt),EntityYaw(e\room\Objects[9]),150.0),0)
											RotateEntity(e\room\Objects[10],CurveAngle(EntityPitch(pvt),EntityPitch(e\room\Objects[10],True),200.0),EntityYaw(e\room\Objects[9]),0,True)
											
											FreeEntity pvt
											
											If FPSfactor > 0 Then ;106:n alle ilmestyy decaleita
												If ((e\EventState-FPSfactor) Mod 100.0)=<50.0 And (e\EventState Mod 100.0)>50.0 Then
													de.Decals = CreateDecal(0, EntityX(Curr106\Collider,True),EntityY(e\room\Objects[3],True)+0.01,EntityZ(Curr106\Collider,True), 90, Rand(360), 0)
													de\Size = 0.2 : de\SizeChange = 0.004 : de\timer = 90000 : EntityAlpha(de\obj, 0.8) : UpdateDecals() 											
												EndIf
											EndIf
										EndIf
									EndIf
									
									dist# = Distance(EntityX(Curr106\Collider),EntityZ(Curr106\Collider),EntityX(e\room\Objects[4],True),EntityZ(e\room\Objects[4],True))
									
									Curr106\CurrSpeed = CurveValue(0, Curr106\CurrSpeed, Max(5*dist,2.0))
									If dist < 15.0 Then
										If e\SoundCHN2 = 0 Then
											TempSound = LoadSound("SFX\106escape.ogg")
											e\SoundCHN2 = PlaySound (TempSound)
										EndIf
										
										If dist<0.4 Then
											Curr106\PathStatus = 0
											Curr106\PathTimer = 70*200
											If Curr106\State3=0 Then 
												SetAnimTime Curr106\obj, 259.0 
												If e\Sound <> 0 Then FreeSound e\Sound
												e\Sound = LoadSound("SFX\Oldman6.ogg")
												e\SoundCHN = PlaySound2(e\Sound, Camera, Curr106\Collider, 35.0)
											EndIf
											
											If FPSfactor > 0 Then ;106:n alle ilmestyy decaleita
												If ((e\EventState-FPSfactor) Mod 160.0)=<50.0 And (e\EventState Mod 160.0)>50.0 Then
													de.Decals = CreateDecal(0, EntityX(Curr106\Collider,True),EntityY(e\room\Objects[3],True)+0.01,EntityZ(Curr106\Collider,True), 90, Rand(360), 0)
													de\Size = 0.05 : de\SizeChange = 0.004 : de\timer = 90000 : EntityAlpha(de\obj, 0.8) : UpdateDecals() 											
												EndIf
											EndIf
											
											DebugLog "anim1: "+AnimTime(Curr106\obj)
											Animate2(Curr106\obj, AnimTime(Curr106\obj), 259, 110, -0.1, False)
											DebugLog "anim2: "+AnimTime(Curr106\obj)
											
											Curr106\State3 = Curr106\State3+FPSfactor
											PositionEntity(Curr106\Collider, EntityX(Curr106\Collider,True),CurveValue(EntityY(e\room\Objects[3],True)-(Curr106\State3/4500.0),EntityY(Curr106\Collider,True),100.0),EntityZ(Curr106\Collider,True))
											If Curr106\State3>700.0 Then
												Curr106\State = 100000
												e\EventState2 = 0
												For i = 5 To 8
													e\room\NPC[i]\State = 2
												Next
												For i = 2 To 4 ;helikopterit hyˆkk‰‰ pelaajan kimppuun
													e\room\NPC[i]\State = 2
												Next
												HideEntity Curr106\obj
											EndIf
										Else
											If dist < 8.5 Then 
												If ChannelPlaying(e\SoundCHN2) = 0 Then
													TempSound = LoadSound("SFX\LightGun.ogg")
													e\SoundCHN2 = PlaySound (TempSound)
													e\EventState2 = 1
												EndIf
												
												If e\EventState2>0 Then
													e\EventState2=e\EventState2+FPSfactor
													If e\EventState2=> 7.5*70 Then
														If e\EventState2-FPSfactor < 7.5*70 Then
															p.Particles = CreateParticle(EntityX(Curr106\obj,True),EntityY(Curr106\obj,True)+0.4, EntityZ(Curr106\obj,True), 4, 7.0, 0, (6.7*70))
															p\speed = 0.0
															p\A = 1.0
															EntityParent p\pvt, Curr106\Collider, True
															
															p.Particles = CreateParticle(EntityX(e\room\Objects[10],True),EntityY(e\room\Objects[10],True),EntityZ(e\room\Objects[10],True), 4, 2.0, 0, (6.7*70))
															RotateEntity p\pvt, EntityPitch(e\room\Objects[10],True),EntityYaw(e\room\Objects[10],True),0,True
															MoveEntity p\pvt, 0, 92.0*RoomScale, 512.0*RoomScale
															p\speed = 0.0
															p\A = 1.0
															EntityParent p\pvt, e\room\Objects[10], True
														ElseIf e\EventState2 < 14.3*70
															CameraShake = 0.5
															LightFlash = 0.3+EntityInView(e\room\Objects[10],Camera)*0.5
														EndIf
													EndIf
												EndIf
												
												For i = 0 To Rand(2,16)-Int(dist)
													p.Particles = CreateParticle(EntityX(Curr106\obj,True),EntityY(Curr106\obj,True)+Rnd(0.4,0.9), EntityZ(Curr106\obj), 0, 0.006, -0.002, 40)
													p\speed = 0.005
													p\A = 0.8
													p\Achange = -0.01
													RotateEntity p\pvt, -Rnd(70,110), Rnd(360),0	
												Next										
											EndIf
											
											
										EndIf
									EndIf
								EndIf
									
								;DebugLog "distance: "+Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\Objects[11],True),EntityZ(e\room\Objects[11],True)) 
								If e\EventState3 = 0.0 Then 
									If Abs(EntityY(Collider)-EntityY(e\room\Objects[11],True))<1.0 Then
										If Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\Objects[11],True),EntityZ(e\room\Objects[11],True)) < 12.0 Then
											Curr106\State = 100000
											HideEntity Curr106\obj
											
											;MTF-ukot tulee tunnelin ovelle
											For i = 5 To 8
												e\room\NPC[i]\State = 3
												PositionEntity e\room\NPC[i]\Collider, EntityX(e\room\Objects[15],True)+(i-6)*0.3,EntityY(e\room\Objects[15],True),EntityZ(e\room\Objects[15],True)+(i-6)*0.3, True
												ResetEntity e\room\NPC[i]\Collider
												
												e\room\NPC[i]\PathStatus = FindPath(e\room\NPC[i], EntityX(Collider),EntityY(Collider)+0.2,EntityZ(Collider))
												e\room\NPC[i]\PathTimer = 70*2
												e\room\NPC[i]\LastSeen = 70*100
											Next
											e\room\NPC[5]\Sound = LoadSound("SFX\MTF\ThereHeIs.ogg")
											PlaySound2(e\room\NPC[5]\Sound, Camera, e\room\NPC[5]\Collider, 25.0)
											
											e\room\RoomDoors[2]\open = True
											
											For i = 2 To 4
												Delete e\room\NPC[i]
												e\room\NPC[i]=Null
											Next
											
											e\EventState3 = 1.0
										EndIf
									EndIf
								ElseIf e\EventState3 = 1.0
									
									For i = 5 To 8
										If EntityDistance(e\room\NPC[i]\Collider,Collider)> 4.0 Then e\room\NPC[i]\State = 3
									Next
									
									If Abs(EntityY(Collider)-EntityY(e\room\Objects[11],True))<1.0 Then
										If Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\Objects[11],True),EntityZ(e\room\Objects[11],True)) < 7.0 Then
											e\room\Objects[12] = LoadMesh("GFX\npcs\s2.b3d")
											EntityColor e\room\Objects[12], 0,0,0
											ScaleMesh (e\room\Objects[12], 0.32/21.3, 0.32/21.3, 0.32/21.3)
											PositionEntity e\room\Objects[12], EntityX(e\room\Objects[11],True), EntityY(e\room\Objects[11],True), EntityZ(e\room\Objects[11],True)
											
											obj = CopyEntity(e\room\Objects[12])
											PositionEntity obj, EntityX(e\room\obj,True)-3968*RoomScale, EntityY(e\room\Objects[11],True), EntityZ(e\room\obj,True)-1920*RoomScale
											
											obj = CopyEntity(e\room\Objects[12])
											PositionEntity obj, EntityX(e\room\obj,True)-4160*RoomScale, EntityY(e\room\Objects[11],True), EntityZ(e\room\obj,True)-1920*RoomScale
											
											obj = CopyEntity(e\room\Objects[12])
											PositionEntity obj, EntityX(e\room\obj,True)-4064*RoomScale, EntityY(e\room\Objects[11],True), EntityZ(e\room\obj,True)-2112*RoomScale
											
											If TempSound <> 0 Then FreeSound TempSound
											TempSound = LoadSound("SFX\Bell.ogg")
											e\SoundCHN = PlaySound2(TempSound, Camera, e\room\Objects[12])
											
											p.Particles = CreateParticle(EntityX(e\room\Objects[11],True),EntityY(Camera,True), EntityZ(e\room\Objects[11],True), 4, 8.0, 0, 50)
											p\speed = 0.15
											p\A = 0.5
											p.Particles = CreateParticle(EntityX(e\room\Objects[11],True),EntityY(Camera,True), EntityZ(e\room\Objects[11],True), 4, 8.0, 0, 50)
											p\speed = 0.25
											p\A = 0.5
											PointEntity p\pvt, Collider
											
											CameraShake = 1.0
											LightFlash = 1.0
											
											e\EventState3 = 2.0
										EndIf
									EndIf
								Else
									e\EventState3=e\EventState3+FPSfactor
									PointEntity e\room\Objects[12], Collider
									RotateEntity e\room\Objects[12], 0, EntityYaw(e\room\Objects[12]), 0
									
									Stamina = -5.0
									
									BlurTimer = Sin(e\EventState3*0.7)*1000.0
									
									If KillTimer = 0 Then 
										CameraZoom(Camera, 1.0+Sin(e\EventState3*0.8)*0.2)
										
										dist = EntityDistance(Collider,e\room\Objects[11])
										If dist < 6.5 Then
											PositionEntity(Collider, CurveValue(EntityX(e\room\Objects[11],True),EntityX(Collider),dist*80),EntityY(Collider),CurveValue(EntityZ(e\room\Objects[0],True),EntityZ(Collider),dist*80))
										EndIf
									EndIf
									
									;tunneli menee umpeen
									If e\EventState3>50 And e\EventState3<230 Then
										CameraShake = Sin(e\EventState3-50)*3
										TurnEntity e\room\Objects[13], 0, Sin(e\EventState3-50)*-0.85, 0, True
										TurnEntity e\room\Objects[14], 0, Sin(e\EventState3-50)*0.85, 0, True
										
										For i = 5 To 8
											PositionEntity (e\room\NPC[i]\Collider, CurveValue(EntityX(e\room\RoomDoors[2]\frameobj,True), EntityX(e\room\NPC[i]\Collider,True),50.0),EntityY(e\room\NPC[i]\Collider,True),CurveValue(EntityZ(e\room\RoomDoors[2]\frameobj,True), EntityZ(e\room\NPC[i]\Collider,True),50.0),True)
											ResetEntity e\room\NPC[i]\Collider
										Next
									EndIf
									
									If e\EventState3=>230.0 Then
										If e\EventState3-FPSfactor<230.0 Then
											If TempSound <> 0 Then FreeSound TempSound
											TempSound = LoadSound("SFX\mst.ogg")
											e\SoundCHN = PlaySound(TempSound)
										EndIf
										
										If ChannelPlaying(e\SoundCHN)=False And SelectedEnding="" Then
											TempSound = LoadSound("SFX\Bell.ogg")
											PlaySound TempSound
											
											p.Particles = CreateParticle(EntityX(e\room\Objects[11],True),EntityY(Camera,True), EntityZ(e\room\Objects[11],True), 4, 8.0, 0, 50)
											p\speed = 0.15
											p\A = 0.5
											p.Particles = CreateParticle(EntityX(e\room\Objects[11],True),EntityY(Camera,True), EntityZ(e\room\Objects[11],True), 4, 8.0, 0, 50)
											p\speed = 0.25
											p\A = 0.5
											
											SelectedEnding = "A1"
											GodMode = 0
											NoClip = 0
											KillTimer = -0.1
											Kill()
										EndIf
										
										If SelectedEnding <> "" Then
											CameraShake=CurveValue(2.0,CameraShake,10.0)
											LightFlash = CurveValue(2.0,LightFlash,8.0);Min(Abs(KillTimer)/100.0,1.0)
										EndIf
										
									EndIf
								EndIf
								
							Else ;contained106=true
								
								If e\EventState2 = 0 Then
									PositionEntity (e\room\NPC[5]\Collider, EntityX(e\room\obj,True)-3408*RoomScale, EntityY(e\room\obj,True)-796*RoomScale, EntityZ(e\room\obj,True)+4976, True)
									ResetEntity e\room\NPC[5]\Collider
									e\EventState2 = 1
								EndIf
								
							EndIf
						EndIf
						
					EndIf
				EndIf
			Case "gateaentrance"
				If PlayerRoom = e\room Then 
					
					Local gatea.Rooms =Null
					For r.Rooms = Each Rooms
						If r\RoomTemplate\Name = "gatea" Then
							gatea = r 
							Exit
						EndIf
					Next
					
					e\EventState = UpdateElevators(e\EventState, e\room\RoomDoors[0], gatea\RoomDoors[1], e\room\Objects[0], e\room\Objects[1], e)
					If Contained106 = False Then 
						If e\EventState < -1.5 And e\EventState+FPSfactor=> -1.5 Then
							PlaySound(OldManSFX(3))
						EndIf
					EndIf
					
					If EntityDistance(Collider, e\room\Objects[1])<4.0 Then
						gatea\RoomDoors[1]\locked = True
						PlayerRoom = gatea
						Delete e
					EndIf
				EndIf
			Case "room2doors173"
				If e\EventState = 0 Then
					If PlayerRoom = e\room Then
						If (Not EntityInView(Curr173\obj, Camera)) Then
							e\EventState = 1
							PositionEntity(Curr173\Collider, EntityX(e\room\Objects[0], True), 0.5, EntityZ(e\room\Objects[0], True))
							ResetEntity(Curr173\Collider)
							Delete e
						EndIf
					EndIf
				EndIf
			Case "buttghost"
				If PlayerRoom = e\room Then
					If EntityDistance(Collider, e\room\Objects[0]) < 1.8 Then
						Achievements(Achv789) = True
						e\SoundCHN = PlaySound2(ButtGhostSFX, Camera,e\room\Objects[0])
						Delete e
					End If
				End If
			Case "682roar"
				If e\EventState = 0 Then
					If PlayerRoom = e\room Then e\EventState = 70 * Rand(300,1000)
				Else
					e\EventState = e\EventState-FPSfactor
					
					If e\EventState < 17*70 Then
						If	e\EventState+FPSfactor => 17*70 Then Roar682SFX = LoadSound("SFX\roar.ogg") : e\SoundCHN = PlaySound(Roar682SFX)
						If e\EventState > 17*70 - 3*70 Then CameraShake = 0.5
						If e\EventState < 17*70 - 7.5*70 And e\EventState > 17*70 - 11*70 Then CameraShake = 2.0				
						If e\EventState < 70 Then FreeSound (Roar682SFX) : Delete e
					EndIf
				EndIf
			Case "testroom173"
				If PlayerRoom = e\room Then	
					If e\EventState = 0 Then
						PositionEntity(Curr173\Collider, EntityX(e\room\Objects[0], True), 0.5, EntityZ(e\room\Objects[0], True))
						ResetEntity(Curr173\Collider)
						e\EventState = 1
					Else
						e\EventState = e\EventState+1
						dist# = EntityDistance(Collider, e\room\Objects[1])
						If dist <1.0 Then
							e\EventState = Max(e\EventState, 70*12)
						ElseIf dist > 1.4
							If e\EventState > 70*12 Then
								If BlinkTimer =< -10 Then
									PlaySound2(GlassBreakSFX, Camera, Curr173\obj) 
									FreeEntity(e\room\Objects[2])
									PositionEntity(Curr173\Collider, EntityX(e\room\Objects[1], True), 0.5, EntityZ(e\room\Objects[1], True))
									ResetEntity(Curr173\Collider)
									Delete e
								EndIf
							EndIf	
						EndIf
						
					End If
				End If	
			Case "testroom"
				If e\EventState = 0 Then
					If PlayerRoom = e\room Then
						If EntityDistance(Collider, e\room\Objects[6]) < 2.5 Then
							Msg079% = LoadSound("SFX\msg.ogg")
							e\SoundCHN = PlaySound(Msg079)
							For i = 0 To 5
								em.Emitters = CreateEmitter(EntityX(e\room\Objects[i], True), EntityY(e\room\Objects[i], True), EntityZ(e\room\Objects[i], True), 0)
								TurnEntity(em\Obj, 90, 0, 0, True)
								;entityParent(em\obj, e\room\obj)
								em\RandAngle = 5
								em\Speed = 0.042
								em\SizeChange = 0.0025									
							Next
							e\EventState = 1
							Delete e
						EndIf
					End If
				Else
					If e\SoundCHN = 0 Then
						FreeSound Msg079
						Delete e						
					Else
						If ChannelPlaying(e\SoundCHN)=0 Then 
							FreeSound Msg079
							Delete e
						EndIf
					EndIf
				End If
			Case "room079"
				If PlayerRoom = e\room Then
					
					If e\EventState = 0 Then
						Music(4) = LoadSound("SFX\Music\AI.ogg")
						e\room\NPC[0]=CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[2],True), EntityY(e\room\Objects[2],True)+0.5, EntityZ(e\room\Objects[2],True))
						PointEntity e\room\NPC[0]\Collider, e\room\obj
						RotateEntity e\room\NPC[0]\Collider, 0, EntityYaw(e\room\NPC[0]\Collider),0, True
						SetAnimTime e\room\NPC[0]\obj, 28
						e\room\NPC[0]\State = 8						
						
						e\EventState = 1
					EndIf
					
					ShouldPlay = 4
					If RemoteDoorOn Then 
						If e\room\RoomDoors[0]\openstate > 50 And e\room\RoomDoors[0]\open Then 
							e\room\RoomDoors[0]\open = False
						EndIf
					ElseIf e\EventState < 10000
						If e\EventState = 1 Then 
							e\EventState = 2
						ElseIf e\EventState = 2
							If EntityDistance(e\room\Objects[0], Collider)<2.5 Then 
								Achievements(Achv079) = True
								e\EventState = 3
								e\EventState2 = 1
								e\Sound = LoadSound("SFX\079_1.ogg")
								e\SoundCHN = PlaySound (e\Sound)
							EndIf							
						ElseIf e\EventState = 3
							If e\EventState < 3500 Then 
								If ChannelPlaying(e\SoundCHN) Then 
									If Rand(3) = 1 Then
										EntityTexture(e\room\Objects[1], OldAiPics(0))
										ShowEntity (e\room\Objects[1])
									ElseIf Rand(10) = 1 
										HideEntity (e\room\Objects[1])							
									End If							
								Else
									If e\Sound <> 0 Then FreeSound e\Sound : e\Sound = 0
									EntityTexture(e\room\Objects[1], OldAiPics(1))
									ShowEntity (e\room\Objects[1])
								EndIf
							Else
								If EntityDistance(e\room\Objects[0], Collider)<2.5 Then 
									e\EventState = 10001
									e\Sound = LoadSound("SFX\079_2.ogg")
									e\SoundCHN = PlaySound (e\Sound)
									EntityTexture(e\room\Objects[1], OldAiPics(1))
									ShowEntity (e\room\Objects[1])								
								EndIf
							EndIf
							
						EndIf
							
					EndIf
					
				EndIf
				
				If e\EventState2 > 0 Then
					If e\EventState2 > 350 Then
						If e\EventState2 < 360 Then ;kun ovi avataan ensimm‰isen kerran, toistetaan ‰‰ni
							e\EventState2=Min(e\EventState2+FPSfactor,355)
							If RemoteDoorOn Then 	
								e\Sound = LoadSound("SFX\079_3.ogg")
								e\SoundCHN = PlaySound (e\Sound)						
								e\EventState2 = 400
								
								For r.Rooms = Each Rooms
									If r\RoomTemplate\Name = "exit1" Then
										r\RoomDoors[4]\open = True
										Exit
									EndIf
								Next
							EndIf							
						Else ;ovi avattu kerran, suljetaan uudestaan jos pelaaja laittaa remotedoorin pois p‰‰lt‰
							e\EventState2=e\EventState2+FPSfactor
							If e\EventState2 > 800 Then
								For r.Rooms = Each Rooms
									If r\RoomTemplate\Name = "exit1" Then
										r\RoomDoors[4]\open = RemoteDoorOn
										Exit
									EndIf
								Next
								e\EventState2 = 400
							EndIf
						EndIf
					Else
						e\EventState2=e\EventState2+FPSfactor
					EndIf
				EndIf				
			Case "room2nuke"
				If PlayerRoom = e\room Then
					e\EventState2 = UpdateElevators(e\EventState2, e\room\RoomDoors[0], e\room\RoomDoors[1], e\room\Objects[4], e\room\Objects[5], e)
					
					e\EventState = UpdateLever(e\room\Objects[1])
					UpdateLever(e\room\Objects[3])
					
				EndIf
			Case "exit1"
				If PlayerRoom = e\room Then
					
					If EntityY(Collider)>1040.0*RoomScale Then
						
						Curr106\State = 20000
						Curr106\Idle = True
						
						If e\EventState = 0 Then
							DrawLoading(0,True)
							
							PositionEntity Sky1, EntityX(e\room\obj,True),EntityY(Sky1,True),EntityZ(e\room\obj,True), True
							RotateEntity Sky1, 0,EntityYaw(e\room\obj,True),0,True
							PositionEntity Sky2, EntityX(e\room\obj,True),EntityY(Sky2,True),EntityZ(e\room\obj,True), True
							RotateEntity Sky2, 0,EntityYaw(e\room\obj,True),0,True
							
							For i = 0 To 19
								If e\room\LightSprites[i]<>0 Then 
									EntityFX e\room\LightSprites[i], 1+8
								EndIf
							Next
							
							Music(5) = LoadSound("SFX\Music\Satiate Strings.ogg")
							DrawLoading(20,True)
							Music(6) = LoadSound("SFX\Music\Medusa.ogg")
							DrawLoading(30,True)
							
							e\room\NPC[1] = CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[4],True),EntityY(e\room\Objects[4],True)+0.5,EntityZ(e\room\Objects[4],True))
							e\room\NPC[1]\State = 1
							
							e\EventState = 1.0
							
							For i = 0 To 70
								DrawLoading((30+i),True)
								a = MilliSecs()+5
								Repeat 
									
								Until MilliSecs()>a
							Next
						Else
							If e\EventState < 2.0 Then 
								If e\room\NPC[0]\State = 2 Then
									ShouldPlay = 6
								Else
									e\EventState2=(e\EventState2+FPSfactor) Mod 3600
									PositionEntity(e\room\NPC[0]\Collider, EntityX(e\room\obj,True)+Cos(e\EventState2/10)*6000.0*RoomScale,14000*RoomScale,EntityZ(e\room\obj,True)+Sin(e\EventState2/10)*6000.0*RoomScale)
									RotateEntity e\room\NPC[0]\Collider,7.0,(e\EventState2/10),20.0											
									ShouldPlay = 5
								EndIf
								
								If EntityDistance(Collider, e\room\Objects[10])<320*RoomScale Then
									e\EventState = 2.0
									e\room\RoomDoors[2]\open = False
									e\room\RoomDoors[2]\locked = 6
									e\room\RoomDoors[3]\open = False
									e\room\RoomDoors[3]\locked = 6
									
									e\room\NPC[2] = CreateNPC(NPCtypeApache, EntityX(e\room\Objects[9],True),EntityY(e\room\Objects[9],True)+0.5,EntityZ(e\room\Objects[9],True))
									e\room\NPC[2]\State = 3
									
									e\room\NPC[3] = CreateNPC(NPCtypeApache, EntityX(e\room\Objects[7],True),EntityY(e\room\Objects[7],True)-2.0,EntityZ(e\room\Objects[7],True))
									e\room\NPC[3]\State = 3
									
									e\room\NPC[0]\State = 3
									
									TempSound = LoadSound("SFX\682battle.ogg")
									e\SoundCHN = PlaySound (TempSound)
								EndIf								
							Else
								ShouldPlay = 6
								e\EventState=e\EventState+FPSfactor
									
								If e\EventState < 40.0*70 Then 	
									e\room\NPC[0]\EnemyX = EntityX(e\room\Objects[11],True)+Sin(MilliSecs()/25.0)*3
									e\room\NPC[0]\EnemyY = EntityY(e\room\Objects[11],True)+Cos(MilliSecs()/85.0)+9.0
									e\room\NPC[0]\EnemyZ = EntityZ(e\room\Objects[11],True)+Cos(MilliSecs()/25.0)*3
									
									e\room\NPC[2]\EnemyX = EntityX(e\room\Objects[11],True)+Sin(MilliSecs()/23.0)*3
									e\room\NPC[2]\EnemyY = EntityY(e\room\Objects[11],True)+Cos(MilliSecs()/83.0)+5.0
									e\room\NPC[2]\EnemyZ = EntityZ(e\room\Objects[11],True)+Cos(MilliSecs()/23.0)*3
									
									If e\room\NPC[3]\State = 3 Then 
										e\room\NPC[3]\EnemyX = EntityX(e\room\Objects[11],True)+Sin(MilliSecs()/20.0)*3
										e\room\NPC[3]\EnemyY = EntityY(e\room\Objects[11],True)+Cos(MilliSecs()/80.0)+3.5
										e\room\NPC[3]\EnemyZ = EntityZ(e\room\Objects[11],True)+Cos(MilliSecs()/20.0)*3
									EndIf
								EndIf
							EndIf
							
							
							If e\EventState > 0.6*70 And e\EventState < 42.2*70 Then 
								If e\EventState < 0.7*70 Then
									CameraShake = 0.5
								ElseIf e\EventState > 3.2*70 And e\EventState < 3.3*70	
									CameraShake = 0.5
								ElseIf e\EventState > 6.1*70 And e\EventState < 6.2*70	
									CameraShake = 0.5
								ElseIf e\EventState < 10.8*70 And e\EventState < 10.9*70	
									CameraShake = 0.5
								ElseIf e\EventState > 12.1*70 And e\EventState < 12.3*70
									CameraShake = 1.0
								ElseIf e\EventState > 13.3*70 And e\EventState < 13.5*70
									CameraShake = 1.5
								ElseIf e\EventState > 16.5*70 And e\EventState < 18.5*70
									CameraShake = 3.0
								ElseIf e\EventState > 21.5*70 And e\EventState < 24.0*70	
									CameraShake = 2.0
								ElseIf e\EventState > 25.5*70 And e\EventState < 27.0*70	
									CameraShake = 2.0	
								ElseIf e\EventState > 31.0*70 And e\EventState < 31.5*70	
									CameraShake = 0.5	
								ElseIf e\EventState > 35.0*70 And e\EventState < 36.5*70	
									CameraShake = 1.5		
									If e\EventState-FPSfactor =< 35.0*70 Then
										If TempSound2 <> 0 Then FreeSound TempSound2 : TempSound2 = 0
										TempSound2 = LoadSound("SFX\nuke1.ogg")
										e\SoundCHN2 = PlaySound (TempSound2)
									EndIf									
								ElseIf e\EventState > 39.5*70 And e\EventState < 39.8*70		
									CameraShake = 1.0
								ElseIf e\EventState > 42.0*70
									CameraShake = 0.5
									
									;helikopterit l‰htee pois
									e\room\NPC[0]\EnemyX = EntityX(e\room\Objects[19],True)+4.0
									e\room\NPC[0]\EnemyY = EntityY(e\room\Objects[19],True)+4.0
									e\room\NPC[0]\EnemyZ = EntityZ(e\room\Objects[19],True)+4.0
									
									e\room\NPC[2]\EnemyX = EntityX(e\room\Objects[19],True)
									e\room\NPC[2]\EnemyY = EntityY(e\room\Objects[19],True)
									e\room\NPC[2]\EnemyZ = EntityZ(e\room\Objects[19],True)
									
								EndIf
							EndIf
							
							If e\EventState => 45.0*70 Then
								If e\EventState < 75.0*70 Then 
									If NuclearSirenSFX = 0 Then NuclearSirenSFX = LoadSound("SFX\nukesiren.ogg")
									If e\SoundCHN = 0 Then
										e\SoundCHN = PlaySound(NuclearSirenSFX)
									Else
										If ChannelPlaying(e\SoundCHN)=False Then e\SoundCHN = PlaySound(NuclearSirenSFX) 
									EndIf
								Else
									If SelectedEnding = "" Then
										If ChannelPlaying(e\SoundCHN)=False Then 
											temp = True
											For e2.Events = Each Events
												If e2\EventName = "room2nuke" Then
													temp = e2\EventState
													Exit
												EndIf
											Next
											
											If temp = 1 Then ;nuken kaukolaukaisu p‰‰ll‰, r‰j‰ytet‰‰n
												ExplosionTimer = Max(ExplosionTimer, 0.1)
												SelectedEnding = "B2"
											Else
												e\Sound = LoadSound("SFX\nuke2.ogg")
												e\SoundCHN = PlaySound(e\Sound)
												
												n.NPCs = CreateNPC(NPCtypeApache, EntityX(e\room\Objects[9],True),EntityY(e\room\Objects[9],True)+0.5,EntityZ(e\room\Objects[9],True))
												n\State = 2
												
												e\room\NPC[2]\State = 2
												
												For i = 14 To 17
													em.Emitters = CreateEmitter(EntityX(e\room\Objects[i],True),EntityY(e\room\Objects[i],True), EntityZ(e\room\Objects[i],True),0)
													TurnEntity(em\Obj, 90, 0, 0, True)
													;EntityParent(em\Obj, e\room\obj)
													em\Room = PlayerRoom
													em\RandAngle = 15
													em\Speed = 0.025
													em\SizeChange = 0.005
													em\Achange = -0.008
													em\Gravity = -0.21
												Next
												
												SelectedEnding = "B3"
											EndIf
											
										EndIf										
									Else
										DebugLog "ewrgdsfgfdghjgfhj"
										If SelectedEnding = "B3" Then
											DebugLog "qwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww"
											e\room\NPC[0]\EnemyX = EntityX(e\room\Objects[11],True)+Sin(MilliSecs()/25.0)*3
											e\room\NPC[0]\EnemyY = EntityY(e\room\Objects[11],True)+Cos(MilliSecs()/85.0)+9.0
											e\room\NPC[0]\EnemyZ = EntityZ(e\room\Objects[11],True)+Cos(MilliSecs()/25.0)*3
											
											e\room\NPC[2]\EnemyX = EntityX(e\room\Objects[11],True)+Sin(MilliSecs()/23.0)*3
											e\room\NPC[2]\EnemyY = EntityY(e\room\Objects[11],True)+Cos(MilliSecs()/83.0)+5.0
											e\room\NPC[2]\EnemyZ = EntityZ(e\room\Objects[11],True)+Cos(MilliSecs()/23.0)*3
											
											If e\EventState-FPSfactor < 80.0*70 And e\EventState => 80.0*70 Then
												For i = 0 To 1
													n.NPCs = CreateNPC(NPCtypeMTF, EntityX(e\room\Objects[18],True)+(i*0.4),EntityY(e\room\Objects[18],True)+0.29*(i+1),EntityZ(e\room\Objects[18],True)+(i*0.4))
													n\State2 = 1000
													n\State = 2
												Next
												
												n.NPCs = CreateNPC(NPCtypeMTF, EntityX(e\room\RoomDoors[2]\obj,True),EntityY(e\room\RoomDoors[2]\obj,True)+0.29,(EntityZ(e\room\RoomDoors[2]\obj,True)+EntityZ(e\room\RoomDoors[3]\obj,True))/2)
												n\State2 = 1000
												n\State = 2		
												
												e\EventState = 85.0*70
											EndIf
										EndIf
									EndIf
									
									
								EndIf
							EndIf
							
							If e\EventState > 26.5*70 Then
								If e\room\Objects[12] = 0 Then
									e\room\Objects[12] = LoadMesh("GFX\NPCs\682arm.b3d")
									ScaleEntity e\room\Objects[12], 0.15,0.15,0.15
									temp = (Min(((EntityDistance(e\room\NPC[3]\Collider,Collider)/RoomScale)-3000.0)/4,1000)+12192.0)*RoomScale
									PositionEntity e\room\Objects[12],EntityX(e\room\NPC[3]\Collider),12192.0*RoomScale,EntityZ(e\room\NPC[3]\Collider)
									RotateEntity e\room\Objects[12],0,e\room\angle+Rnd(-10,10),0,True
									TurnEntity e\room\Objects[12], 0,0,180
								Else
									If WrapAngle(EntityRoll(e\room\Objects[12]))<340.0 Then 
										angle# = WrapAngle(EntityRoll(e\room\Objects[12]))
										TurnEntity e\room\Objects[12], 0,0,(5.0+Abs(Sin(angle))*2)*FPSfactor
										If angle < 270 And WrapAngle(EntityRoll(e\room\Objects[12]))=> 270 Then
											If TempSound2 <> 0 Then FreeSound TempSound2 : TempSound2 = 0
											TempSound2 = LoadSound("SFX\apachecrash1.ogg")
											PlaySound TempSound2
											e\room\NPC[3]\State = 4
											e\room\NPC[3]\State2 = 1.0
											e\room\NPC[3]\EnemyX = EntityX(e\room\Objects[7],True)
											e\room\NPC[3]\EnemyY = EntityY(e\room\Objects[7],True)-2.5
											e\room\NPC[3]\EnemyZ = EntityZ(e\room\Objects[7],True)
											
											em.Emitters = CreateEmitter(EntityX(e\room\NPC[3]\Collider), EntityY(e\room\NPC[3]\Collider), EntityZ(e\room\NPC[3]\Collider),0)
											em\Room = PlayerRoom
											em\RandAngle = 45
											em\Gravity = -0.18
											em\LifeTime = 400
											em\SizeChange = Rnd(0.005,0.007)
											em\Achange = -0.004
											TurnEntity(em\Obj, -80+20*i, 0, 0)
											EntityParent em\Obj, e\room\NPC[3]\Collider
												
											For i = 0 To 7
												p.Particles = CreateParticle(EntityX(e\room\NPC[3]\Collider),EntityY(e\room\NPC[3]\Collider),EntityZ(e\room\NPC[3]\Collider), 0, Rnd(0.5,1.0), -0.1, 200)
												p\speed = 0.01
												p\SizeChange = 0.01
												p\A = 1.0
												p\Achange = -0.005
												RotateEntity p\pvt, Rnd(360),Rnd(360),0
												MoveEntity p\pvt, 0,0,0.3
											Next
											
											For i = 0 To 12
												p.Particles = CreateParticle(EntityX(e\room\NPC[3]\Collider),EntityY(e\room\NPC[3]\Collider),EntityZ(e\room\NPC[3]\Collider), 0, 0.02, 0.003, 200)
												p\speed = 0.04
												p\A = 1.0
												p\Achange = -0.005
												RotateEntity p\pvt, Rnd(360),Rnd(360),0
											Next
										EndIf
									Else
										HideEntity e\room\Objects[12]
									EndIf
								EndIf
							EndIf
							
							
							;0.5
							;2.1
							;3.3
							;6.5 - 8.5
							;11.5-14
							;15.5-17
							;21
							;25-26.5
							;29.5
							;32
						EndIf
						
						;dist = Max(Min(EntityDistance(Collider, e\room\objects[3])/10000.0,1.0),0.0)
						;EntityAlpha Fog, 1.0-dist
						HideEntity Fog
						CameraFogRange Camera, 5,45
						CameraFogColor (Camera,200,200,200)
						CameraClsColor (Camera,200,200,200)					
						CameraRange(Camera, 0.05, 60)
						
						If Rand(3) = 1 Then
							p.Particles = CreateParticle(EntityX(Camera)+Rnd(-2.0,2.0), EntityY(Camera)+Rnd(0.9,2.0), EntityZ(Camera)+Rnd(-2.0,2.0), 2, 0.006, 0, 300)
							p\speed = Rnd(0.002,0.003)
							RotateEntity(p\pvt, Rnd(-20, 20), e\room\angle-90+Rnd(-15,15),0, 0)
							
							p\SizeChange = -0.00001
							
							FreeEntity pvt
						End If
						
						If Rand(250)=1 And e\room\NPC[1]\State <> 1 Then 
							If e\room\NPC[1]\PathStatus = 0 Then
								If EntityDistance(e\room\NPC[1]\Collider, e\room\Objects[4])<EntityDistance(e\room\NPC[1]\Collider, e\room\Objects[5]) Then
									e\room\NPC[1]\PathStatus = FindPath(e\room\NPC[1], EntityX(e\room\Objects[5],True),EntityY(e\room\Objects[5],True),EntityZ(e\room\Objects[5],True))
									;DebugLog "<   -   "+e\room\NPC[1]\PathStatus
									e\room\NPC[1]\State = 3
								Else
									e\room\NPC[1]\PathStatus = FindPath(e\room\NPC[1], EntityX(e\room\Objects[4],True),EntityY(e\room\Objects[4],True),EntityZ(e\room\Objects[4],True))
									DebugLog "- > - "+e\room\NPC[1]\PathStatus
									e\room\NPC[1]\State = 3
								EndIf
							EndIf
						EndIf	
						
						PositionTexture(e\room\Objects[1], e\EventState/1500.0, 0)
						PositionTexture(e\room\Objects[2], e\EventState/2500.0, 0)				
						;TurnEntity e\room\objects[1], 0, 0.1*FPSfactor, 0
						;TurnEntity e\room\objects[2], 0, 0.1*FPSfactor, 0, True		
						
						;helikopteri huomaa pelaajan -> ilmoittaa vartijoille
						If EntityVisible(e\room\NPC[0]\Collider,Collider) Then
							e\room\NPC[1]\State = 1
						EndIf
						
					Else
						e\EventState2 = UpdateElevators(e\EventState2, e\room\RoomDoors[0], e\room\RoomDoors[1], e\room\Objects[8], e\room\Objects[9], e)
						
						EntityAlpha Fog, 1.0
						
						If Rand(20)=1 Then
							;Music(5) = LoadSound("SFX\Music\Satiate Strings.ogg")
							;Music(6) = LoadSound("SFX\Music\Medusa.ogg")	
						EndIf
					EndIf
					
				EndIf
			Case "room3servers"
				If PlayerRoom = e\room Then
					If e\EventState3=0 Then
						If BlinkTimer < -10 Then 
							temp = Rand(0,2)
							PositionEntity Curr173\Collider, EntityX(e\room\Objects[temp],True),EntityY(e\room\Objects[temp],True),EntityZ(e\room\Objects[temp],True)
							ResetEntity Curr173\Collider
							e\EventState3=1
						EndIf
					EndIf
					
					If e\room\Objects[3]>0 Then 
						If BlinkTimer<-8 And BlinkTimer >-12 Then
							PointEntity e\room\Objects[3], Camera
							RotateEntity(e\room\Objects[3], 0, EntityYaw(e\room\Objects[3],True),0, True)
						EndIf
						If e\EventState2 = 0 Then 
							e\EventState = CurveValue(0, e\EventState, 15.0)
							If Rand(800)=1 Then e\EventState2 = 1
						Else
							e\EventState = e\EventState+(FPSfactor*0.5)
							If e\EventState > 360 Then e\EventState = 0	
							
							If Rand(1200)=1 Then e\EventState2 = 0
						EndIf
						
						PositionEntity e\room\Objects[3], EntityX(e\room\Objects[3],True), (-608.0*RoomScale)+0.05+Sin(e\EventState+270)*0.05, EntityZ(e\room\Objects[3],True), True
					EndIf
				EndIf
			Case "room2tesla"				temp = True
				If e\EventState2 > 70*3.5 And e\EventState2 < 70*90 Then temp = False
				
				If PlayerRoom = e\room And temp Then
					
					If e\EventState = 0 Then
						If e\SoundCHN = 0 Then ;soitetaan huminaa jos pelaaja ei l‰hell‰
							e\SoundCHN = PlaySound2(TeslaIdleSFX, Camera, e\room\Objects[3],4.0,0.5)
						Else
							If Not ChannelPlaying(e\SoundCHN) Then e\SoundCHN = PlaySound2(TeslaIdleSFX, Camera, e\room\Objects[3],4.0,0.5)
						EndIf
						
						For i = 0 To 2
							If Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True)) < 300.0*RoomScale Then
								;soitetaan laukaisu‰‰ni
								If KillTimer => 0 Then 
									PlayerSoundVolume = Max(8.0,PlayerSoundVolume)
									StopChannel(e\SoundCHN)
									e\SoundCHN = PlaySound2(TeslaActivateSFX, Camera, e\room\Objects[3],4.0,0.5)
									e\EventState = 1
									Exit
								EndIf
							EndIf
						Next
						
						If Curr106\State < -10 And e\EventState = 0 Then 
							For i = 0 To 2
								If Distance(EntityX(Curr106\Collider),EntityZ(Curr106\Collider),EntityX(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True)) < 300.0*RoomScale Then
									;soitetaan laukaisu‰‰ni
									If KillTimer => 0 Then 
										StopChannel(e\SoundCHN)
										e\SoundCHN = PlaySound2(TeslaActivateSFX, Camera, e\room\Objects[3],4.0,0.5)
										e\EventState = 1
										Achievements(AchvTesla) = True
										Exit
									EndIf
								EndIf
							Next						
						EndIf
					Else
						e\EventState = e\EventState+FPSfactor
						If e\EventState > 40 Then
							If e\EventState-FPSfactor =< 40 Then PlaySound(IntroSFX(11))	
							If e\EventState < 70 Then 
								
								If KillTimer => 0 Then 
									For i = 0 To 2
										If Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True)) < 250.0*RoomScale Then
											ShowEntity Light
											LightFlash = 0.4
											CameraShake = 1.0
											Kill()
										EndIf
									Next
								EndIf
								
								If Curr106\State < -10 Then
									For i = 0 To 2
										If Distance(EntityX(Curr106\Collider),EntityZ(Curr106\Collider),EntityX(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True)) < 250.0*RoomScale Then
											ShowEntity Light
											LightFlash = 0.3
											For i = 0 To 10
												p.Particles = CreateParticle(EntityX(Curr106\Collider, True), EntityY(Curr106\Collider, True), EntityZ(Curr106\Collider, True), 0, 0.015, -0.2, 250)
												p\size = 0.03
												p\gravity = -0.2
												p\lifetime = 200
												p\SizeChange = 0.005
												p\speed = 0.001
												RotateEntity(p\pvt, Rnd(360), Rnd(360), 0, True)
											Next
											Curr106\State = -20000
											TranslateEntity(Curr106\Collider,0,-50.0,0,True)
										EndIf
									Next								
								EndIf
								
								HideEntity e\room\Objects[3]
								
								If Rand(5)<5 Then 
									PositionTexture TeslaTexture,0.0,Rnd(0,1.0)
									ShowEntity e\room\Objects[3]								
								EndIf
							Else 
								If e\EventState-FPSfactor < 70 Then 
									StopChannel(e\SoundCHN)	
									e\SoundCHN = PlaySound2(TeslaPowerUpSFX, Camera, e\room\Objects[3],4.0,0.5)
								EndIf 
								HideEntity e\room\Objects[3]
								If e\EventState > 150 Then e\EventState = 0
							EndIf
						EndIf
					EndIf
				EndIf
				
				If e\EventState2 = 0 Then
					If e\EventState3 <=0 Then 
						temp = False
						For n.NPCs = Each NPCs
							If n\NPCtype = NPCtypeMTF Then
								If Abs(EntityX(n\Collider)-EntityX(e\room\obj,True))<4.0 Then
									If Abs(EntityZ(n\Collider)-EntityZ(e\room\obj,True))<4.0 Then
										temp = True
										If e\EventState2 = 0 Then
											n\Sound = LoadSound("SFX\MTF\Tesla0.ogg")
											PlaySound2(n\Sound, Camera, n\Collider)
											PlayMTFMessage(n\Sound)
											e\Sound = LoadSound("SFX\MTF\Tesla1.ogg")
											e\SoundCHN = PlaySound (e\Sound)
											n\Idle = 70*10
											e\EventState2 = 70*100
										EndIf
									EndIf
								EndIf
							EndIf
						Next
						If temp = False Then e\EventState2=70*3.5
						e\EventState3=e\EventState3+140
					Else
						e\EventState3=e\EventState3-FPSfactor
					EndIf
				Else
					e\EventState2 = Max(e\EventState2-FPSfactor,0)
				EndIf
			Case "pj"
				If e\EventState = 0 Then
					If PlayerRoom = e\room Then
						If EntityDistance(Collider, e\room\obj) < 2.5 Then
							PlaySound(RustleSFX(Rand(0,2)))
							CreateNPC(NPCtype372, 0, 0, 0)
							e\EventState = 1
							Delete e
						EndIf					
					EndIf
				EndIf
			Case "room2tunnel"	
				If (Not Contained106) Then 
				
					If e\EventState = 0 Then
						If PlayerRoom = e\room Then e\EventState = 1
					ElseIf e\EventState = 1
						If EntityDistance(Collider, e\room\Objects[0]) < 0.9 Then
							de.Decals = CreateDecal(0, EntityX(Collider,True), -2493.0*RoomScale, EntityZ(Collider,True), 90, Rand(360), 0)
							de\Size = 0.05 : de\SizeChange = 0.001 : EntityAlpha(de\obj, 0.8) : UpdateDecals
							
							Curr106\State = -0.1
							e\EventState = 2
						ElseIf EntityDistance(Collider, e\room\Objects[1]) < 0.9 Then
							de.Decals = CreateDecal(0, EntityX(Collider,True), -2493.0*RoomScale, EntityZ(Collider,True), 90, Rand(360), 0)
							de\Size = 0.05 : de\SizeChange = 0.001 : EntityAlpha(de\obj, 0.8) : UpdateDecals
							
							PositionEntity Curr106\Collider, EntityX(e\room\Objects[1],True), EntityY(Curr106\Collider), EntityZ(e\room\Objects[1],True)
							Curr106\State = -0.1
							e\EventState = 3
						EndIf
					ElseIf e\EventState = 2
						dist = Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\Objects[1], True), EntityZ(e\room\Objects[1], True))
						If dist < 1.0 Then
							pvt% = CreatePivot()
							PositionEntity(pvt, EntityX(Collider), EntityY(Collider), EntityZ(Collider))
							PointEntity(pvt, e\room\Objects[1])
							RotateEntity(pvt, 0, EntityYaw(pvt), 0)
							MoveEntity(pvt, 0, 0, dist + 3.0)
							PositionEntity(Curr106\Collider, EntityX(pvt), EntityY(pvt), EntityZ(pvt))
							FreeEntity pvt
							
							Curr106\SoundTimer = 0
							e\EventState = 4
						EndIf
					ElseIf e\EventState = 3
						dist = Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\Objects[0], True), EntityZ(e\room\Objects[0], True))
						
						If dist < 1.0 Then
							pvt% = CreatePivot()
							PositionEntity(pvt, EntityX(Collider), EntityY(Collider), EntityZ(Collider))
							PointEntity(pvt, e\room\Objects[0])
							RotateEntity(pvt, 0, EntityYaw(pvt), 0)
							MoveEntity(pvt, 0, 0, dist + 3.0)
							PositionEntity(Curr106\Collider, EntityX(pvt), EntityY(pvt), EntityZ(pvt))
							FreeEntity pvt
							
							Curr106\SoundTimer = 0
							e\EventState = 4
						EndIf
					EndIf
					
				EndIf
				
				If PlayerRoom = e\room Then
					e\EventState2 = UpdateElevators(e\EventState2, e\room\RoomDoors[0], e\room\RoomDoors[1],e\room\Objects[2],e\room\Objects[3], e)
					
					e\EventState3 = UpdateElevators(e\EventState3, e\room\RoomDoors[2], e\room\RoomDoors[3],e\room\Objects[4],e\room\Objects[5], e)
				EndIf 
			Case "room049"				If PlayerRoom = e\room Then
					e\EventState2 = UpdateElevators(e\EventState2, e\room\RoomDoors[0], e\room\RoomDoors[1],e\room\Objects[0],e\room\Objects[1], e)
					
					e\EventState3 = UpdateElevators(e\EventState3, e\room\RoomDoors[2], e\room\RoomDoors[3],e\room\Objects[2],e\room\Objects[3], e)
					
					If EntityY(Collider) < -2848*RoomScale Then
						If e\EventState = 0 Then
							n.NPCs = CreateNPC(NPCtypeZombie, EntityX(e\room\Objects[6],True),EntityY(e\room\Objects[6],True),EntityZ(e\room\Objects[6],True))
							PointEntity n\Collider, e\room\obj
							TurnEntity n\Collider, 0, -40, 0
							n.NPCs = CreateNPC(NPCtypeZombie, EntityX(e\room\Objects[8],True),EntityY(e\room\Objects[8],True),EntityZ(e\room\Objects[8],True))
							PointEntity n\Collider, e\room\obj
							TurnEntity n\Collider, 0, 20, 0
							e\EventState=1
						ElseIf e\EventState > 0
							If e\EventState < 70*190 Then 
								e\EventState = Min(e\EventState+FPSfactor,70*190)
								;049 spawns after 3 minutes or when the player is close enough to the containment chamber
								If e\EventState > 70*180 Or EntityDistance(Collider, e\room\Objects[8])<800*RoomScale Then
									e\Sound = LoadSound("SFX\Horror13.ogg")
									PlaySound(e\Sound)
									
									For n.NPCs = Each NPCs ;awake the zombies
										If n\NPCtype = NPCtypeZombie Then
											n\State = 1
										EndIf
									Next
									n.NPCs = CreateNPC(NPCtype049, EntityX(e\room\Objects[5],True), EntityY(e\room\Objects[5],True), EntityZ(e\room\Objects[5],True))
									n\State = 2
									e\room\NPC[0]=n
									e\room\RoomDoors[4]\open = True
									PlaySound2(OpenDoorSFX(Rand(0,2)),Camera, e\room\RoomDoors[4]\obj, 6.0)
									
									e\EventState= 70*190
								EndIf
							ElseIf e\EventState < 70*220
								If e\room\NPC[0]=Null Then
									For n.NPCs = Each NPCs
										If n\NPCtype=NPCtype049 Then e\room\NPC[0]=n : Exit
									Next
								Else
									If EntityDistance(e\room\NPC[0]\Collider,Collider)<4.0 Then
										e\EventState=e\EventState+FPSfactor
										If e\EventState > 70*195 And e\EventState-FPSfactor =< 70*195 Then
											TempSound=LoadSound("SFX\049\049_"+Rand(1,2)+".ogg")
											PlaySound2(TempSound,Camera, e\room\NPC[0]\Collider)
										ElseIf e\EventState > 70*204 And e\EventState-FPSfactor =< 70*204
											TempSound=LoadSound("SFX\049\049_"+Rand(3,5)+".ogg")
											PlaySound2(TempSound,Camera, e\room\NPC[0]\Collider)
										ElseIf e\EventState > 70*217 And e\EventState-FPSfactor =< 70*217
											TempSound=LoadSound("SFX\049\049_"+Rand(6,7)+".ogg")
											PlaySound2(TempSound,Camera, e\room\NPC[0]\Collider)
											e\EventState=70*221
										EndIf
									EndIf
								EndIf
							EndIf
						Else ;e\eventstate < 0
							
							
							If e\EventState > -70*4 Then 
								If FallTimer => 0 Then 
									FallTimer = Min(-1, FallTimer)
									PositionEntity(Head, EntityX(Camera, True), EntityY(Camera, True), EntityZ(Camera, True), True)
									ResetEntity (Head)
									RotateEntity(Head, 0, EntityYaw(Camera) + Rand(-45, 45), 0)
								ElseIf FallTimer < -230
									FallTimer = -231
									BlinkTimer = 0
									e\EventState = e\EventState-FPSfactor
									
									If e\EventState =< -70*4 Then 
										UpdateDoors()
										UpdateRooms()
										ShowEntity Collider
										DropSpeed = 0
										BlinkTimer = -10
										FallTimer = 0
										PositionEntity Collider, EntityX(e\room\obj,True), EntityY(e\room\Objects[6],True)+0.2, EntityZ(e\room\obj,True)
										ResetEntity Collider										
										
										PositionEntity e\room\NPC[0]\Collider, EntityX(e\room\Objects[0],True),EntityY(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True),True
										ResetEntity e\room\NPC[0]\Collider
										
										For n.NPCs = Each NPCs
											If n\NPCtype = NPCtypeZombie Then
												PositionEntity n\Collider, EntityX(e\room\Objects[0],True),EntityY(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True),True
												ResetEntity n\Collider
											EndIf
										Next
										
										n.NPCs = CreateNPC(NPCtypeMTF, EntityX(e\room\Objects[6],True), EntityY(e\room\Objects[6],True)+0.2, EntityZ(e\room\Objects[6],True))
										n\State = 2
										n\LastSeen = (70*35)
										n\Reload = 6*70
										n\State3 = 70*145
										e\room\NPC[1]=n
										
										PointEntity Collider, e\room\NPC[1]\Collider
										
										TempSound = LoadSound("SFX\049\MTF_1.ogg")
										PlaySound TempSound
										e\Sound = LoadSound("SFX\zombiebreath.ogg")
									EndIf
								EndIf
							Else
								BlurTimer = 800
								ForceMove = 0.5
								Injuries = Max(2.0,Injuries)
								Bloodloss = 0
								MoveLock = True
								PointEntity Collider, e\room\NPC[1]\Collider
								If KillTimer < 0 Then
									If ChannelPlaying(e\room\NPC[1]\SoundChn) Then StopChannel(e\room\NPC[1]\SoundChn)
									TempSound = LoadSound("SFX\049\MTF_2.ogg")
									PlaySound TempSound
									Delete e
								Else
									If e\SoundCHN = 0 Then
										e\SoundCHN = PlaySound (e\Sound)
									Else
										If (Not ChannelPlaying(e\SoundCHN)) Then e\SoundCHN = PlaySound(e\Sound)
									EndIf
								EndIf
							EndIf
						EndIf
					EndIf
				EndIf 
			Case "008"
				If PlayerRoom = e\room Then	
					
					Achievements(Achv008)=True
					
					If e\EventState = 0 Then
						If EntityDistance(Collider, e\room\Objects[0])<2.0 Then
							e\EventState=1
						EndIf
					Else
						If Not ChannelPlaying(e\SoundCHN) Then e\SoundCHN = PlaySound2(AlarmSFX(0), Camera, e\room\Objects[0], 6.0)
						
						e\EventState=Min(e\EventState+FPSfactor,100)
						RotateEntity(e\room\Objects[1],Sin(Min(90,e\EventState))*89.0,EntityYaw(e\room\Objects[1],True),0,True)
						If e\EventState > 90 And e\room\Objects[4]=0 Then
							
							em.Emitters = CreateEmitter(EntityX(e\room\Objects[0],True),EntityY(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True), 1)
							TurnEntity(em\Obj, -90, 0, 0)
							e\room\Objects[4] = em\Obj
							em\RandAngle = 26
							em\SizeChange = 0.01
							em\Achange = -0.015
							em\Gravity = -0.12
							
							For i = 0 To 7
								p.Particles = CreateParticle(EntityX(e\room\Objects[0],True),EntityY(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True), 6, Rnd(0.2,0.3), -0.1, 200)
								p\speed = 0.01
								p\SizeChange = 0.01
								p\A = 0.8
								p\Achange = -0.01
								RotateEntity p\pvt, Rnd(360),Rnd(360),0
								
								p.Particles = CreateParticle(EntityX(e\room\Objects[0],True),EntityY(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True), 2, 0.002, 0.1, 200)
								p\speed = 0.1
								p\A = 1.0
								RotateEntity p\pvt, -Rnd(180),Rnd(360),0
							Next
							If (Not WearingHazmat) Then 
								Injuries=Injuries+0.1
								Infect=1
								Msg = "Something flew off and cut your arm"
								MsgTimer = 70*8
							EndIf
							
							PlaySound2(GlassBreakSFX, Camera, e\room\Objects[0]) 
						EndIf
						
					End If
				End If
			Case "room012"
				If PlayerRoom = e\room Then
					If e\EventState=0 Then
						If EntityDistance(Collider, e\room\RoomDoors[0]\obj)<2.5 Then
							Achievements(Achv012)=True
							PlaySound HorrorSFX(7)
							PlaySound2 (LeverSFX,Camera,e\room\RoomDoors[0]\obj) 
							e\EventState=1
							e\room\RoomDoors[0]\locked = False
							UseDoor(e\room\RoomDoors[0],False)
							e\room\RoomDoors[0]\locked = True
						EndIf
					Else
						
						If e\Sound=0 Then e\Sound=LoadSound("SFX\012\On Mount Golgotha.ogg")
						e\SoundCHN = LoopSound2(e\Sound, e\SoundCHN, Camera, e\room\Objects[3], 5.0)
						
						If e\Sound2=0 Then e\Sound2=LoadSound("SFX\012\ambient.ogg")
						
						If e\EventState<90 Then e\EventState=CurveValue(90,e\EventState,500)
						PositionEntity e\room\Objects[2], EntityX(e\room\Objects[2],True),(-130-448*Sin(e\EventState))*RoomScale,EntityZ(e\room\Objects[2],True),True
						
						If e\EventState2 > 0 And e\EventState2 < 200 Then
							e\EventState2 = e\EventState2 + FPSfactor
							RotateEntity(e\room\Objects[1], CurveValue(85, EntityPitch(e\room\Objects[1]), 5), EntityYaw(e\room\Objects[1]), 0)
						Else
							e\EventState2 = e\EventState2 + FPSfactor
							If e\EventState2<250 Then
								ShowEntity e\room\Objects[3] 
							Else
								HideEntity e\room\Objects[3] 
								If e\EventState2>300 Then e\EventState2=200
							EndIf
						EndIf
						
						If Wearing714=False Then
							temp = False
							If EntityVisible(e\room\Objects[2],Camera) Then temp = True
							
							;012 not visible, walk to the door
							If temp=False Then
								If EntityVisible(e\room\RoomDoors[0]\frameobj,Camera) Then
									pvt% = CreatePivot()
									PositionEntity pvt, EntityX(Camera), EntityY(Collider), EntityZ(Camera)
									PointEntity(pvt, e\room\RoomDoors[0]\frameobj)
									;TurnEntity(pvt, 90, 0, 0)
									user_camera_pitch = CurveAngle(90, user_camera_pitch+90, 100)
									user_camera_pitch=user_camera_pitch-90
									RotateEntity(Collider, EntityPitch(Collider), CurveAngle(EntityYaw(pvt), EntityYaw(Collider), 150), 0)
									
									DebugLog "door - "+EntityYaw(pvt)+" - "+EntityYaw(Collider)
									
									angle = WrapAngle(EntityYaw(pvt)-EntityYaw(Collider))
									If angle<40.0 Then
										ForceMove = (40.0-angle)*0.008
									ElseIf angle > 310.0
										ForceMove = (40.0-Abs(360.0-angle))*0.008
									EndIf
									
									FreeEntity pvt										
								EndIf
							Else
								e\SoundCHN2 = LoopSound2(e\Sound2, e\SoundCHN2, Camera, e\room\Objects[3], 10, e\EventState3/(86.0*70.0))
								
								DebugLog "012"
								pvt% = CreatePivot()
								PositionEntity pvt, EntityX(Camera), EntityY(e\room\Objects[2],True)-0.05, EntityZ(Camera)
								PointEntity(pvt, e\room\Objects[2])
								RotateEntity(Collider, EntityPitch(Collider), CurveAngle(EntityYaw(pvt), EntityYaw(Collider), 80-(e\EventState3/200.0)), 0)
								
								TurnEntity(pvt, 90, 0, 0)
								user_camera_pitch = CurveAngle(EntityPitch(pvt)+25, user_camera_pitch + 90.0, 80-(e\EventState3/200.0))
								user_camera_pitch=user_camera_pitch-90
								
								dist = Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\Objects[2],True),EntityZ(e\room\Objects[2],True))
								
								HeartBeatRate = 150
								HeartBeatVolume = Max(3.0-dist,0.0)/3.0
								BlurVolume = Max((2.0-dist)*(e\EventState3/800.0)*(Sin(Float(MilliSecs()) / 20.0 + 1.0)),BlurVolume)
								CurrCameraZoom = Max(CurrCameraZoom, (Sin(Float(MilliSecs()) / 20.0)+1.0)*8.0*Max((3.0-dist),0.0))
								
								If dist < 0.6 Then
									DebugLog "dist < 0.7"
									e\EventState3=Min(e\EventState3+FPSfactor,86*70)
									If e\EventState3>70 And e\EventState3-FPSfactor=<70 Then
										TempSound=LoadSound("SFX\012\player1.ogg")
										PlaySound TempSound
									ElseIf e\EventState3>13*70 And e\EventState3-FPSfactor=<13*70
										Msg="You start pushing your nails through your wrist"
										MsgTimer = 7*70
										Injuries=Injuries+0.5
										TempSound=LoadSound("SFX\012\player2.ogg")
										PlaySound TempSound
									ElseIf e\EventState3>31*70 And e\EventState3-FPSfactor=<31*70
										tex = LoadTexture("GFX\map\scp-012_1.jpg")
										EntityTexture (e\room\Objects[4], tex,0,1)
										
										Msg="You tear open your left wrist and start completing the composition with your blood"
										MsgTimer = 7*70
										Injuries=Max(Injuries,1.5)
										TempSound=LoadSound("SFX\012\player"+Rand(3,4)+".ogg")
										PlaySound TempSound	
									ElseIf e\EventState3>49*70 And e\EventState3-FPSfactor=<49*70
										Msg="You push your fingers deeper into the wound"
										MsgTimer = 8*70
										Injuries=Injuries+0.3
										TempSound=LoadSound("SFX\012\player5.ogg")
										PlaySound TempSound	
									ElseIf e\EventState3>63*70 And e\EventState3-FPSfactor=<63*70
										tex = LoadTexture("GFX\map\scp-012_2.jpg")
										EntityTexture (e\room\Objects[4], tex,0,1)		
										
										Injuries=Injuries+0.5
										TempSound=LoadSound("SFX\012\player6.ogg")
										PlaySound TempSound
									ElseIf e\EventState3>74*70 And e\EventState3-FPSfactor=<74*70
										tex = LoadTexture("GFX\map\scp-012_3.jpg")
										EntityTexture (e\room\Objects[4], tex,0,1)
										
										Msg="You rip the wound wide open"
										MsgTimer = 7*70
										Injuries=Injuries+0.8
										TempSound=LoadSound("SFX\012\player7.ogg")
										PlaySound TempSound
										Crouch = True
										
										de.Decals = CreateDecal(17,  EntityX(Collider), -768*RoomScale+0.01, EntityZ(Collider),90,Rnd(360),0)
										de\Size = 0.1 : de\maxsize = 0.45 : de\sizechange = 0.0002 : UpdateDecals()
									ElseIf e\EventState3>85*70 And e\EventState3-FPSfactor=<85*70	
										Kill()
									EndIf
									
									RotateEntity(Collider, EntityPitch(Collider), CurveAngle(EntityYaw(Collider)+Sin(e\EventState3*(e\EventState3/2000))*(e\EventState3/300), EntityYaw(Collider), 80), 0)
									
								Else
									angle = WrapAngle(EntityYaw(pvt)-EntityYaw(Collider))
									If angle<40.0 Then
										ForceMove = (40.0-angle)*0.02
									ElseIf angle > 310.0
										ForceMove = (40.0-Abs(360.0-angle))*0.02
									EndIf
									DebugLog "a: "+dist+" - "+angle
								EndIf								
								
								FreeEntity pvt								
							EndIf
							
						EndIf
						
						
						
					EndIf
				EndIf
			Case "room2pipes106"	
				If (Not Contained106) Then 
					If e\EventState = 0 Then
						If PlayerRoom = e\room Then e\EventState = 1
					Else
						e\EventState=(e\EventState+FPSfactor*0.7)
						;0-50 = menee keskelle
						;50-200 = katselee ymp‰rilleen
						;200-250 = l‰htee pois
						
						
						If e\EventState < 50 Then
							Curr106\Idle = True
							PositionEntity(Curr106\Collider, EntityX(e\room\Objects[0], True), EntityY(Collider) - 0.15, EntityZ(e\room\Objects[0], True))
							PointEntity(Curr106\Collider, e\room\Objects[1])
							MoveEntity(Curr106\Collider, 0, 0, EntityDistance(e\room\Objects[0], e\room\Objects[1])*0.5 * (e\EventState / 50.0))
							Animate2(Curr106\obj, AnimTime(Curr106\obj), 284, 333, 0.02*35)
						ElseIf e\EventState < 200
							Curr106\Idle = True
							;DebugLog AnimTime(Curr106\obj) + AnimLength(Curr106\obj)
							Animate2(Curr106\obj, AnimTime(Curr106\obj), 334, 494, 0.2)
							
							;DebugLog AnimTime(Curr106\obj)
							PositionEntity(Curr106\Collider, (EntityX(e\room\Objects[0], True)+EntityX(e\room\Objects[1], True))/2, EntityY(Collider) - 0.15, (EntityZ(e\room\Objects[0], True)+EntityZ(e\room\Objects[1], True))/2)
							;MoveEntity(Curr106\Collider, 0, 0, EntityDistance(e\room\Objects[0], e\room\Objects[1])*0.5)
							RotateEntity(Curr106\Collider,0, CurveValue(e\EventState,EntityYaw(Curr106\Collider),30.0),0,True)
							If EntityDistance(Curr106\Collider, Collider)<4.0 Then
								pvt = CreatePivot()
								PositionEntity(pvt, EntityX(Curr106\Collider),EntityY(Curr106\Collider),EntityZ(Curr106\Collider))
								PointEntity pvt, Collider
								If WrapAngle(EntityYaw(pvt)-EntityYaw(Curr106\Collider))<80 Then
									Curr106\State = -11
									Curr106\Idle = False
									PlaySound(HorrorSFX(10))
									e\EventState = 260
								EndIf
								FreeEntity pvt
							EndIf
						ElseIf e\EventState < 250
							Curr106\Idle = True
							PositionEntity(Curr106\Collider, EntityX(e\room\Objects[0], True), EntityY(Collider) - 0.15, EntityZ(e\room\Objects[0], True))
							PointEntity(Curr106\Collider, e\room\Objects[1])
							;200-250     (- 150)      50-100
							MoveEntity(Curr106\Collider, 0, 0, EntityDistance(e\room\Objects[0], e\room\Objects[1]) * ((e\EventState-150.0) / 100.0))
							Animate2(Curr106\obj, AnimTime(Curr106\obj), 284, 333, 0.02*35)
						EndIf
						ResetEntity(Curr106\Collider)
						
						;PositionEntity(Curr106\Collider, EntityX(Curr106\Collider), EntityY(Collider) - 0.20, EntityZ(Curr106\Collider))
						
						If (e\EventState / 250.0) > 0.3 And ((e\EventState - FPSfactor*0.7) / 250.0) <= 0.3 Then
							e\SoundCHN = PlaySound(HorrorSFX(6))
							BlurTimer = 800
							d.Decals = CreateDecal(0, EntityX(e\room\Objects[2], True), EntityY(e\room\Objects[2], True), EntityZ(e\room\Objects[2], True), 0, e\room\angle - 90, Rnd(360)) ;90, Rnd(360), 0
							d\Timer = 90000
							d\Alpha = 0.01 : d\AlphaChange = 0.005
							d\Size = 0.1 : d\SizeChange = 0.003
						EndIf
						
						If (e\EventState / 250.0) > 0.65 And ((e\EventState - FPSfactor*0.7) / 250.0) <= 0.65 Then
							d.Decals = CreateDecal(0, EntityX(e\room\Objects[3], True), EntityY(e\room\Objects[3], True), EntityZ(e\room\Objects[3], True), 0, e\room\angle + 90, Rnd(360))
							d\Timer = 90000
							d\Alpha = 0.01 : d\AlphaChange = 0.005
							d\Size = 0.1 : d\SizeChange = 0.003
						EndIf						
						
						If e\EventState > 250 Then Curr106\Idle = False : Delete e
						
					End If
				EndIf
			Case "room2pit106"
				If (Not Contained106) Then 
					If e\EventState = 0 Then
						If PlayerRoom = e\room Then e\EventState = 1
					Else
						e\EventState = e\EventState + 1
						PositionEntity(Curr106\Collider, EntityX(e\room\Objects[7], True), EntityY(e\room\Objects[7], True), EntityZ(e\room\Objects[7], True))
						ResetEntity(Curr106\Collider)
						
						PointEntity(Curr106\Collider, Camera)
						TurnEntity(Curr106\Collider, 0, Sin(MilliSecs() / 20) * 6.0, 0, True)
						MoveEntity(Curr106\Collider, 0, 0, Sin(MilliSecs() / 15) * 0.06)
						
						Curr106\Idle = True
						
						If e\EventState > 800 Then
							If BlinkTimer < - 5 Then Curr106\Idle = False : Delete e
						EndIf
					EndIf
				End If
			Case "room2pit"
				If e\EventState =< 0 Then
					e\EventState = Rand(30,35)
					If Abs(EntityX(Collider)-EntityX(e\room\obj)) < 8.0 Then
						If Abs(EntityZ(Collider)-EntityZ(e\room\obj)) < 8.0 Then			
							If (Not EntityVisible(Curr173\Collider, Camera)) And (Not EntityVisible(e\room\Objects[6], Camera)) Then 
								PositionEntity(Curr173\Collider, EntityX(e\room\Objects[6], True), 0.5, EntityZ(e\room\Objects[6], True))
								ResetEntity(Curr173\Collider)
								Delete e
							EndIf
						EndIf
					End If
				Else
					e\EventState = e\EventState-FPSfactor
				End If
			Case "toiletguard"
				If e\EventState = 0 Then
					If e\EventState2 <= 0 Then 
						e\EventState2 = 70*2
						If Abs(EntityX(e\room\obj)-EntityX(Collider))<6.0 Then
							If Abs(EntityZ(e\room\obj)-EntityZ(Collider))<6.0 Then
								e\EventState = 1
							EndIf
						EndIf
					Else
						e\EventState2=e\EventState2-FPSfactor
					EndIf
				ElseIf e\EventState = 1
					e\room\NPC[0]=CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[1],True), EntityY(e\room\Objects[1],True)+0.5, EntityZ(e\room\Objects[1],True))
					PointEntity e\room\NPC[0]\Collider, e\room\obj
					RotateEntity e\room\NPC[0]\Collider, 0, EntityYaw(e\room\NPC[0]\Collider)-20,0, True
					SetAnimTime e\room\NPC[0]\obj, 28
					e\room\NPC[0]\State = 8
					
					e\EventState = 2	
				Else
					If e\Sound = 0 Then e\Sound = LoadSound("SFX\SuicideGuard1.ogg")
					e\SoundCHN = LoopSound2(e\Sound, e\SoundCHN, Camera, e\room\Objects[1], 15.0)
					
					If EntityDistance(Collider, e\room\obj)<4.0 And PlayerSoundVolume > 1.0 Then
						de.Decals = CreateDecal(3,  EntityX(e\room\Objects[2],True), 0.01, EntityZ(e\room\Objects[2],True),90,Rnd(360),0)
						de\Size = 0.3 : ScaleSprite (de\obj, de\size, de\size)
						
						de.Decals = CreateDecal(17,  EntityX(e\room\Objects[2],True), 0.01, EntityZ(e\room\Objects[2],True),90,Rnd(360),0)
						de\Size = 0.1 : de\maxsize = 0.45 : de\sizechange = 0.0002 : UpdateDecals()
						
						StopChannel e\SoundCHN
						FreeSound e\Sound
						
						e\Sound = LoadSound("SFX\SuicideGuard2.ogg")
						e\SoundCHN = PlaySound2(e\Sound, Camera, e\room\Objects[1], 15.0)
						
						Delete e
					EndIf
				EndIf
			Case "room3tunnel"
				If e\EventState = 0 Then
					e\room\NPC[0]=CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[0],True), EntityY(e\room\Objects[0],True)+0.5, EntityZ(e\room\Objects[0],True))
					PointEntity e\room\NPC[0]\Collider, e\room\obj
					RotateEntity e\room\NPC[0]\Collider, 0, EntityYaw(e\room\NPC[0]\Collider)+Rnd(-20,20),0, True
					SetAnimTime e\room\NPC[0]\obj, 28
					e\room\NPC[0]\State = 8
					
					it = CreateItem("SCP-500-01", "scp500", e\room\x, 0.5, e\room\z)
					EntityParent(it\obj, e\room\obj)
					
					e\EventState = 1
					Delete e
				EndIf
			Case "tunnel2smoke"
				If e\EventState = 0 Then
					If Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\obj), EntityZ(e\room\obj)) < 3.5 Then
						PlaySound2(BurstSFX, Camera, e\room\obj) 
						For i = 0 To 1
							em.Emitters = CreateEmitter(EntityX(e\room\Objects[i],True),EntityY(e\room\Objects[i],True), EntityZ(e\room\Objects[i],True),0)
							TurnEntity(em\Obj, 90, 0, 0, True)
							EntityParent(em\Obj, e\room\obj)
							em\RandAngle = 3
							em\Speed = 0.04
							em\SizeChange = 0.0027
							;EntityParent(em\Obj, e\room\obj)
							
							For z = 0 To 10
								p.Particles = CreateParticle(EntityX(em\Obj, True), 448*RoomScale, EntityZ(em\Obj, True), Rand(em\MinImage, em\MaxImage), em\Size, em\Gravity, em\LifeTime)
								p\speed = em\Speed
								RotateEntity(p\pvt, Rnd(360), Rnd(360), 0, True)
								
								p\SizeChange = em\SizeChange
							Next
							
						Next
						Delete e
						
					End If
				End If			
			Case "tunnel2"
				If PlayerRoom = e\room Then 
					If e\EventState = 0 Then
						If Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\obj), EntityZ(e\room\obj)) < 3.5 Then
							PlaySound(LightSFX)
							
							LightBlink = Rnd(0.0,1.0)*(e\EventState/200)
							e\EventState = 1
						End If
					ElseIf e\EventState < 200
						
						BlinkTimer = -10
						If e\EventState > 30 Then 
							LightBlink = 1.0 
							If e\EventState-FPSfactor =< 30 Then 
								If AmbientSFX(4)=0 Then AmbientSFX(4) = LoadSound("SFX\ambient\ambient5.ogg")
								PlaySound AmbientSFX(4)
							EndIf
						EndIf
						If e\EventState-FPSfactor =< 100 And e\EventState > 100 Then 
							If AmbientSFX(10)=0 Then AmbientSFX(10) = LoadSound("SFX\ambient\ambient11.ogg")
							PlaySound AmbientSFX(10)
							PositionEntity(Curr173\Collider, EntityX(e\room\obj), 0.6, EntityZ(e\room\obj))
							ResetEntity(Curr173\Collider)					
							Curr173\Idle = True				
						EndIf
						LightBlink = 1.0
						e\EventState = e\EventState + FPSfactor
					Else
						BlinkTimer = BLINKFREQ
						
						Curr173\Idle = False
						Delete e
					End If		
				EndIf
			Case "coffin"
				dist = EntityDistance(Camera, e\room\Objects[0])
				CoffinDistance = dist	
				
				If CoffinDistance < 1.5 Then Achievements(Achv895) = True	
				
				If e\EventState = 0 Then
					e\room\NPC[0]=CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[1],True), EntityY(e\room\Objects[1],True)+0.5, EntityZ(e\room\Objects[1],True))
					PointEntity e\room\NPC[0]\Collider, e\room\obj
					RotateEntity e\room\NPC[0]\Collider, 0, EntityYaw(e\room\NPC[0]\Collider)+Rnd(-10,10),0, True
					SetAnimTime e\room\NPC[0]\obj, 28
					e\room\NPC[0]\State = 8
					
					e\EventState = 1
				EndIf
			Case "coffin106"
				dist = EntityDistance(Camera, e\room\Objects[0])
				CoffinDistance = dist
				
				If e\EventState = 0 Then
					e\room\NPC[0]=CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[1],True), EntityY(e\room\Objects[1],True)+0.5, EntityZ(e\room\Objects[1],True))
					PointEntity e\room\NPC[0]\Collider, e\room\obj
					RotateEntity e\room\NPC[0]\Collider, 0, EntityYaw(e\room\NPC[0]\Collider)+Rnd(-10,10),0, True
					SetAnimTime e\room\NPC[0]\obj, 28
					
					e\room\NPC[0]\State = 8
					
					e\EventState = 1
				EndIf
				
				If e\EventState = 1 Then
					If PlayerRoom = e\room Then
						If ShouldPlay = 0 Then ShouldPlay = 66
					Else
						If ShouldPlay = 66 Then ShouldPlay = 0
					End If
					
					If CoffinDistance < 1.5 Then
						Achievements(Achv895) = True
						If (Not Contained106) Then
							de.Decals = CreateDecal(0, EntityX(Collider,True), -1531.0*RoomScale, EntityZ(Collider,True), 90, Rand(360), 0)
							de\Size = 0.05 : de\SizeChange = 0.001 : EntityAlpha(de\obj, 0.8) : UpdateDecals()
							
							Curr106\State = -0.1
							e\EventState = 2
						EndIf
					EndIf
				EndIf
			Case "tunnel106"
				If (Not Contained106) Then
					If e\EventState = 0 Then
						If Curr106\State >= 0 And Rand(10)=1 Then
							If EntityDistance(Camera, e\room\obj) < 5.0 Then
								e\EventState = 1
							EndIf
						EndIf
					ElseIf e\EventState = 1
						dist = Distance(EntityX(e\room\obj),EntityZ(e\room\obj),EntityX(Collider),EntityZ(Collider))
						
						If dist < 3.0 Or Rand(7000)=1 Then
							e\EventState = 2
							d.Decals = CreateDecal(0, EntityX(e\room\obj), 445.0*RoomScale, EntityZ(e\room\obj), -90, Rand(360), 0)
							d\Size = Rnd(0.5, 0.7) : EntityAlpha(d\obj, 0.7) : d\ID = 1 : ScaleSprite(d\obj, d\Size, d\Size)
							EntityAlpha(d\obj, Rnd(0.7, 0.85))
							
							PlaySound HorrorSFX(10)
						ElseIf dist > 8.0
							If Rand(5) = 1 Then
								Curr106\Idle = False
								Delete e
							Else
								Curr106\Idle = False
								Curr106\State = -10000
								Delete e
							End If
						EndIf
					Else
						e\EventState = e\EventState+FPSfactor
						
						PositionEntity(Curr106\Collider, EntityX(e\room\obj, True) - Sin(MilliSecs() / 150.0) / 4.0, EntityY(Collider) + 1.0 - Min(Sin(e\EventState)*1.5,1.1), EntityZ(e\room\obj, True) - Sin(MilliSecs() / 190.0) / 4.0)
						
						;TranslateEntity(Curr106\Collider, 0, -Max((3.0-dist),0), 0, True)
						PointEntity(Curr106\Collider, Camera)
						Curr106\State = -11
						Animate2(Curr106\obj, AnimTime(Curr106\obj), 55, 104, 0.1)
						Curr106\Idle = True
						
						If e\EventState > 180 Then
							Curr106\Idle = False
							PositionEntity(Curr106\Collider, EntityX(Curr106\Collider), -3.0, EntityZ(Curr106\Collider), True)
							
							Delete e
						EndIf
							
					EndIf
				EndIf
			Case "lockroom173"
				If e\EventState =< 0 Then
					e\EventState = Rand(30,35)
					If Abs(EntityX(Collider)-EntityX(e\room\obj)) < 6.0 Then
						If Abs(EntityZ(Collider)-EntityZ(e\room\obj)) < 6.0 Then			
							If (Not EntityInView(Curr173\Collider, Camera)) Or EntityDistance(Curr173\Collider, Collider)>15.0 Then 
								PositionEntity(Curr173\Collider, e\room\x + Cos(225-90 + e\room\angle) * 2, e\room\y + 1.0, e\room\z + Sin(225-90 + e\room\angle) * 2)
								ResetEntity(Curr173\Collider)
								Delete e
							EndIf
						EndIf
					End If
				Else
					e\EventState = e\EventState-FPSfactor
				End If
			Case "lockroom096"
				If PlayerRoom = e\room And e\EventState = 0 Then
					If Curr096=Null Then
						Curr096 = CreateNPC(NPCtype096, EntityX(e\room\obj,True), 0.3, EntityZ(e\room\obj,True))
						RotateEntity Curr096\Collider, 0, e\room\angle+45, 0, True
						e\EventState = 1
						Delete e
					Else
						PositionEntity Curr096\Collider, EntityX(e\room\obj,True), 0.3, EntityZ(e\room\obj,True), True
					EndIf
				End If
			Case "pocketdimension"
				If PlayerRoom = e\room Then
					Achievements(AchvPD) = True
					
					If e\Sound = 0 Then e\Sound = LoadSound("SFX\PDrumble.ogg")
					If e\Sound2 = 0 Then e\Sound2 = LoadSound("SFX\PDbreath.ogg")
					
					If e\EventState = 0 Then
						CameraFogColor Camera, 0,0,0
						CameraClsColor Camera, 0,0,0
						e\EventState = 0.1
					EndIf
					
					If Music(3)=0 Then Music(3) = LoadSound("SFX\Music\PocketDimension.ogg")	
					ShouldPlay = 3
					
					If e\EventState < 600 Then
						BlurTimer = 1000
						BlinkTimer = -10-(e\EventState/60.0)
					EndIf
					
					ScaleEntity(e\room\obj,RoomScale, RoomScale*(1.0 + Sin(e\EventState/14.0)*0.2), RoomScale)
					For i = 0 To 7
						ScaleEntity(e\room\Objects[i],RoomScale*(1.0 + Abs(Sin(e\EventState/21.0+i*45.0)*0.1)),RoomScale*(1.0 + Sin(e\EventState/14.0+i*20.0)*0.1), RoomScale,True)
					Next
					;ScaleEntity(e\room\Objects[8],RoomScale*(1.5 + Abs(Sin(e\EventState/21.0+i*45.0)*0.1)),RoomScale*(1.0 + Sin(e\EventState/14.0+i*20.0)*0.1), RoomScale,True)
					ScaleEntity(e\room\Objects[9],RoomScale*(1.5 + Abs(Sin(e\EventState/21.0+i*45.0)*0.1)),RoomScale*(1.0 + Sin(e\EventState/14.0+i*20.0)*0.1), RoomScale,True)
					
					e\EventState = e\EventState + FPSfactor
					
					If e\EventState2 = 0 Then 
						If e\EventState > 65*70 Then
							If Rand(800)=1 And Curr106\State =>0 Then	
								PlaySound HorrorSFX(8)
								Curr106\State = -0.1
								e\EventState = 601
							EndIf
						ElseIf Curr106\State > 0 ;106 kiert‰‰ isointa huonetta
							angle = (e\EventState/10 Mod 360)
							PositionEntity(Curr106\Collider, EntityX(e\room\obj), 0.2+0.35+Sin(e\EventState/14.0+i*20.0)*0.4, EntityX(e\room\obj))
							RotateEntity(Curr106\Collider, 0,angle,0)
							MoveEntity(Curr106\Collider,0,0,6.0-Sin(e\EventState/10.0))
							DebugLog "1: "+AnimTime(Curr106\obj)
							Animate2(Curr106\obj, AnimTime(Curr106\obj), 55, 104, 0.5)
							DebugLog "2: "+AnimTime(Curr106\obj)
							DebugLog "----"
							RotateEntity(Curr106\Collider, 0,angle+90,0)
							Curr106\Idle = True
						EndIf
					EndIf 
					
					If EntityDistance(Collider, Curr106\Collider) < 0.3 Then ;106 hyˆkk‰‰ jos on tarpeeksi l‰hell‰ pelaajaa
						Curr106\Idle = False
						Curr106\State = -11
					EndIf
					
					If e\EventState2 = 1 Then ;pieness‰ huoneessa
						dist# = EntityDistance(Collider, e\room\Objects[8])
						
						PositionEntity(e\room\Objects[9], EntityX(e\room\Objects[8],True)+3384*RoomScale, 0.0, EntityZ(e\room\Objects[8],True))
						
						TranslateEntity e\room\Objects[9], Cos(e\EventState)*5, 0, Sin(e\EventState*2)*4, True
						RotateEntity e\room\Objects[9],0,e\EventState * 2,0
						
						PositionEntity(e\room\Objects[10], EntityX(e\room\Objects[8],True), 0.0, EntityZ(e\room\Objects[8],True)+3384*RoomScale)
						
						TranslateEntity e\room\Objects[10], Sin(e\EventState*2)*4, 0, Cos(e\EventState)*5, True
						RotateEntity e\room\Objects[10],0,e\EventState * 2,0
						
						For i = 9 To 10
							dist = Distance(EntityX(Collider), EntityZ(Collider),EntityX(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True))
							If dist<6.0 Then 
								If dist<100.0*RoomScale Then
									pvt=CreatePivot()
									PositionEntity pvt, EntityX(e\room\Objects[i],True),EntityY(Collider),EntityZ(e\room\Objects[i],True)
									
									PointEntity pvt, Collider
									RotateEntity pvt, 0, Int(EntityYaw(pvt)/90)*90,0,True
									MoveEntity pvt, 0,0,100*RoomScale
									PositionEntity Collider, EntityX(pvt),EntityY(Collider),EntityZ(pvt)
									;ResetEntity Collider
									FreeEntity pvt
									
									If KillTimer = 0 Then
										If TempSound<>0 Then FreeSound TempSound
										TempSound=LoadSound("SFX\PDimpact.ogg")
										PlaySound (TempSound)	
										KillTimer=-1.0
									EndIf
								EndIf
								e\SoundCHN = LoopSound2(e\Sound, e\SoundCHN, Camera, e\room\Objects[i], 6.0)	
							EndIf
						
						Next
						
						pvt=CreatePivot()
						PositionEntity pvt, EntityX(e\room\Objects[8],True)-1536*RoomScale,500*RoomScale,EntityZ(e\room\Objects[8],True)+608*RoomScale
						If EntityDistance(pvt, Collider)<3.0 Then 
							e\SoundCHN2 = LoopSound2(e\Sound2, e\SoundCHN2, Camera, pvt, 3.0)
						EndIf
						FreeEntity pvt
						
						;106's eyes
						PositionEntity e\room\Objects[17], EntityX(e\room\Objects[8],True),1024*RoomScale,EntityZ(e\room\Objects[8],True)-2848*RoomScale
						PointEntity e\room\Objects[17], Collider
						TurnEntity e\room\Objects[17], 0, 180, 0
						
						temp = EntityDistance(Collider, e\room\Objects[17])
						If temp < 1500*RoomScale Then
							Injuries = Injuries + (FPSfactor/4000)
							
							Sanity = Max(Sanity - FPSfactor / temp / 8,-1000)
							
							e\SoundCHN = LoopSound2(OldManSFX(4), e\SoundCHN, Camera, e\room\Objects[17], 5.0, 0.6)
							
							CurrCameraZoom = Max(CurrCameraZoom, (Sin(Float(MilliSecs()) / 20.0)+1.0)*15.0*Max((6.0-temp)/6.0,0.0))
							
							pvt% = CreatePivot()
							PositionEntity pvt, EntityX(Camera), EntityY(Camera), EntityZ(Camera)
							PointEntity(pvt, e\room\Objects[17])
							TurnEntity(pvt, 90, 0, 0)
							user_camera_pitch = CurveAngle(EntityPitch(pvt), user_camera_pitch + 90.0, Min(Max(15000.0 / (-Sanity), 15.0), 500.0))
							user_camera_pitch=user_camera_pitch-90
							RotateEntity(Collider, EntityPitch(Collider), CurveAngle(EntityYaw(pvt), EntityYaw(Collider), Min(Max(15000.0 / (-Sanity), 15.0), 500.0)), 0)
							FreeEntity pvt
						EndIf
						
						If EntityY(Collider) < -1600*RoomScale Then
							If dist > 4800*RoomScale Then
								CameraFogColor Camera, 0,0,0
								CameraClsColor Camera, 0,0,0
								
								DropSpeed = 0
								BlurTimer = 500
								PositionEntity(Collider, EntityX(e\room\obj), 0.5, EntityZ(e\room\obj))
								ResetEntity Collider
								e\EventState2 = 0
								UpdateDoors()
								UpdateRooms()
							Else ;pelaaja on jossain muualla -> pudonnut alas
								If KillTimer => 0 Then PlaySound HorrorSFX(8)
								KillTimer = Min(-1, KillTimer)	
								BlurTimer = 3000
							EndIf
						EndIf
						
						If dist > 1700*RoomScale And 0 Then
							BlinkTimer = -10
							Select Rand(2)
								Case 1 ;pienest‰ huoneesta pieneen huoneeseen
									pvt = CreatePivot()
									PositionEntity(pvt, EntityX(Collider), EntityY(Collider), EntityZ(Collider))
									
									PointEntity(pvt, e\room\Objects[8])
									MoveEntity pvt, 0,0,dist*1.9
									PositionEntity(Collider, EntityX(pvt), EntityY(pvt), EntityZ(pvt))
									ResetEntity Collider
									
									angle = Int(EntityYaw(pvt)/90.0)*90
									RotateEntity pvt, 0,angle, 0
									MoveEntity pvt, 0,0,0.8
									PositionEntity(e\room\Objects[10], EntityX(pvt), 0.0, EntityZ(pvt))
									RotateEntity e\room\Objects[10], 0, EntityYaw(pvt), 0, True
									
									FreeEntity pvt
								Case 2 ;pienest‰ huoneesta isoon huoneeseen
									pvt = CreatePivot()
									PositionEntity(pvt, EntityX(e\room\obj), EntityY(e\room\obj), EntityZ(e\room\obj))
									
									angle = WrapAngle(GetAngle(EntityX(e\room\Objects[8]),EntityZ(e\room\Objects[8]),EntityX(Collider),EntityZ(Collider))-22.5)
									angle = Int(pvt/90.0)
									
									RotateEntity(pvt, 0,45.0*Rand(0,8),0)
									MoveEntity pvt, 0,0,1400*RoomScale
									PositionEntity(Collider, EntityX(pvt), EntityY(Collider), EntityZ(pvt))
									RotateEntity Collider, 0, EntityYaw(Collider)-angle*45 - 270, 0, True
									
									MoveEntity pvt, 0,0,0.8
									PositionEntity(e\room\Objects[10], EntityX(pvt), 0.0, EntityZ(pvt))
									RotateEntity e\room\Objects[10], 0, EntityYaw(pvt), 0, True	
									
									ResetEntity Collider
									FreeEntity pvt
									e\EventState2 = 0	
							End Select
							
							UpdateDoors()
							UpdateRooms()
							
						EndIf 
					ElseIf e\EventState2 = 0
						dist# = EntityDistance(Collider, e\room\obj)	
						
						If dist > 1700*RoomScale Then
							BlinkTimer = -10
							
							Select Rand(19)
								Case 1,2,3,4 ;isosta huoneesta isoon huoneeseen
									PlaySound(OldManSFX(3))
									
									pvt = CreatePivot()
									PositionEntity(pvt, EntityX(Collider), EntityY(Collider), EntityZ(Collider))
									
									PointEntity(pvt, e\room\obj)
									MoveEntity pvt, 0,0,dist*1.9
									PositionEntity(Collider, EntityX(pvt), EntityY(Collider), EntityZ(pvt))
									ResetEntity Collider
									
									MoveEntity pvt, 0,0,0.8
									PositionEntity(e\room\Objects[10], EntityX(pvt), 0.0, EntityZ(pvt))
									RotateEntity e\room\Objects[10], 0, EntityYaw(pvt), 0, True	
									
									FreeEntity pvt
								Case 5,6,7,8,9 ;isosta huoneesta pieneen huoneeseen
									e\EventState2=1
									BlinkTimer = -10
									PlaySound(OldManSFX(3))
									
									PositionEntity(Collider, EntityX(e\room\Objects[8],True), 0.5, EntityZ(e\room\Objects[8],True))
									ResetEntity Collider
								Case 10,11 ;ison huoneen keskelle
									BlurTimer = 500
									PositionEntity Collider,EntityX(e\room\obj), 0.5, EntityZ(e\room\obj)
								Case 12,13 ;106:n huoneeseen
									BlurTimer = 1500
									For r.Rooms = Each Rooms
										If r\RoomTemplate\Name = "room106" Then
											DebugLog "106"
											e\EventState = 0
											e\EventState2 = 0
											FreeSound Music(3) : Music(3)=0
											PositionEntity(Collider, EntityX(r\obj), 0.4, EntityZ(r\obj))
											ResetEntity Collider
											Curr106\State = 10000
											Curr106\Idle = False
											Exit
										EndIf
									Next
								Case 14,15
									BlurTimer = 1500
									For r.Rooms = Each Rooms
										If r\RoomTemplate\Name = "room2offices" Then
											DebugLog "room2offices"
											e\EventState = 0
											e\EventState2 = 0
											FreeSound Music(3) : Music(3)=0
											PositionEntity(Collider, EntityX(r\obj), 0.4, EntityZ(r\obj))
											ResetEntity Collider
											Curr106\Idle = False
											Exit
										EndIf
									Next
								Case 16,17,18,19 ;tornihuoneeseen
									DebugLog "tornihuone"
									BlinkTimer = -10
									PositionEntity(Collider, EntityX(e\room\Objects[12],True), 0.6, EntityZ(e\room\Objects[12],True))
									ResetEntity Collider
									e\EventState2 = 15
							End Select 
							
							UpdateDoors()
							UpdateRooms()
						EndIf					
					Else ;tornihuone
						CameraFogColor Camera, 38*0.5, 55*0.5, 47*0.5
						CameraClsColor Camera, 38*0.5, 55*0.5, 47*0.5
						
						If Rand(800)=1 Then 
							angle = EntityYaw(Camera,True)+Rnd(150,210)
							p.Particles = CreateParticle(EntityX(Collider)+Cos(angle)*7.5, 0.0, EntityZ(Collider)+Sin(angle)*7.5, 3, 4.0, 0.0, 2500)
							EntityBlend(p\obj, 2)
							;EntityFX(p\obj, 1)
							p\speed = 0.01
							p\SizeChange = 0
							PointEntity(p\pvt, Camera)
							TurnEntity(p\pvt, 0, 145, 0, True)
							TurnEntity(p\pvt, Rand(10,20), 0, 0, True)
						EndIf
						
						If e\EventState2 > 12 Then 
							Curr106\Idle = True
							PositionEntity(Curr106\Collider, EntityX(e\room\Objects[e\EventState2],True),0.27, EntityZ(e\room\Objects[e\EventState2],True))
							
							PointEntity(Curr106\Collider, Camera)
							TurnEntity(Curr106\Collider, 0, Sin(MilliSecs() / 20) * 6.0, 0, True)
							MoveEntity(Curr106\Collider, 0, 0, Sin(MilliSecs() / 15) * 0.06)
							
							If Rand(750)=1 And e\EventState2 > 12 Then
								BlinkTimer = -10
								e\EventState2 = e\EventState2-1
								PlaySound HorrorSFX(8)
							EndIf
							
							If e\EventState2 = 12 Then
								CameraShake = 1.0
								PositionEntity(Curr106\Collider, EntityX(e\room\Objects[e\EventState2],True),-1.0, EntityZ(e\room\Objects[e\EventState2],True))
								Curr106\State = -11
								ResetEntity Curr106\Collider
							EndIf
							
						Else 
							Curr106\State = -11
							Curr106\Idle = False
						EndIf
						
						If EntityY(Collider) < -1600*RoomScale Then
							;pelaaja on "ulosk‰ynnill‰"
							If Distance(EntityX(e\room\Objects[16],True),EntityZ(e\room\Objects[16],True),EntityX(Collider),EntityZ(Collider))<144*RoomScale Then
								
								CameraFogColor Camera, 0,0,0
								CameraClsColor Camera, 0,0,0
								
								DropSpeed = 0
								BlurTimer = 500
								PositionEntity(Collider, EntityX(e\room\obj), 0.5, EntityZ(e\room\obj))
								ResetEntity Collider
								e\EventState2 = 0
								UpdateDoors()
								UpdateRooms()
							Else ;pelaaja on jossain muualla -> pudonnut alas
								If KillTimer => 0 Then PlaySound HorrorSFX(8)
								KillTimer = Min(-1, KillTimer)	
								BlurTimer = 3000
							EndIf
						EndIf 
						
					EndIf
					
				Else
					CameraClsColor Camera, 0,0,0
					e\EventState = 0
					e\EventState2 = 0
				EndIf
				
				
			Case "room106"
				Local Magnets% = False, FemurBreaker% = False
				
				;eventstate2 = onko magneetit p‰‰ll‰
				
				If SoundTransmission Then 
					If e\EventState = 1 Then
						e\EventState3 = Min(e\EventState3+FPSfactor,4000)
					EndIf
					If ChannelPlaying(e\SoundCHN) = False Then e\SoundCHN = PlaySound(RadioStatic)   
				EndIf
				
				If PlayerRoom = e\room Then
					
					ShowEntity e\room\NPC[0]\obj
					
					ShouldPlay = 66
					
					;DebugLog EntityX(e\room\objects[7], True)+", "+ EntityY(e\room\objects[7], True) +", "+EntityZ(e\room\objects[7], True)
					
					e\room\NPC[0]\State=2
					If e\room\NPC[0]\Idle = 0 Then
						Animate2(e\room\NPC[0]\obj, AnimTime(e\room\NPC[0]\obj), 17.0, 19.0, 0.01, False)
						If AnimTime(e\room\NPC[0]\obj) = 19.0 Then e\room\NPC[0]\Idle = 1
					Else
						Animate2(e\room\NPC[0]\obj, AnimTime(e\room\NPC[0]\obj), 19.0, 17.0, -0.01, False)	
						If AnimTime(e\room\NPC[0]\obj) = 17.0 Then e\room\NPC[0]\Idle = 0
					EndIf
					
					PositionEntity(e\room\NPC[0]\Collider, EntityX(e\room\Objects[5],True),EntityY(e\room\Objects[5],True),EntityZ(e\room\Objects[5],True),True)
					RotateEntity(e\room\NPC[0]\Collider,EntityPitch(e\room\Objects[5],True),EntityYaw(e\room\Objects[5],True),0,True)
					ResetEntity(e\room\NPC[0]\Collider)
					
					temp = e\EventState2
					
					Local leverstate = UpdateLever(e\room\Objects[1],((EntityY(e\room\Objects[6],True)<-990*RoomScale) And (EntityY(e\room\Objects[6],True)>-1275.0*RoomScale)))
					If GrabbedEntity = e\room\Objects[1] And DrawHandIcon = True Then e\EventState2 = leverstate
					
					If e\EventState2 <> temp Then 
						If e\EventState2 = False Then
							PlaySound(MagnetDownSFX)
						Else
							PlaySound(MagnetUpSFX)	
						EndIf
					EndIf
					
					SoundTransmission% = UpdateLever(e\room\Objects[3])
				
					If e\EventState = 0 Then 
						If SoundTransmission And Rand(100)=1 Then
							If e\SoundCHN2 = 0 Then
								TempSound = LoadSound("SFX\LureSubject"+Rand(2,7)+".ogg")
								e\SoundCHN2 = PlaySound(TempSound)								
							EndIf
							If ChannelPlaying(e\SoundCHN2) = False Then
								TempSound = LoadSound("SFX\LureSubject"+Rand(2,7)+".ogg")
								e\SoundCHN2 = PlaySound(TempSound)
							EndIf
						EndIf
						
						UpdateButton(e\room\Objects[4])
						If ClosestButton = e\room\Objects[4] And MouseHit1 Then
							e\EventState = 1 ;femur breaker k‰yntiin
							If SoundTransmission = True Then ;soitetaan ‰‰ni jos sound transmission on p‰‰ll‰
								If e\SoundCHN2 <> 0 Then
									If ChannelPlaying(e\SoundCHN2) Then StopChannel e\SoundCHN2
								EndIf 
								FemurBreakerSFX = LoadSound("SFX\FemurBreaker.ogg")
								e\SoundCHN2 = PlaySound (FemurBreakerSFX)
							EndIf
						EndIf
					ElseIf e\EventState = 1 ;luu murrettu
						If SoundTransmission And e\EventState3 < 2000 Then 
							If e\SoundCHN2 = 0 Then 
								TempSound = LoadSound("SFX\LureSubject1.ogg")
								e\SoundCHN2 = PlaySound(TempSound)								
							EndIf
							If ChannelPlaying(e\SoundCHN2) = False Then
								TempSound = LoadSound("SFX\LureSubject1.ogg")
								e\SoundCHN2 = PlaySound(TempSound)
							EndIf
						EndIf
						
						If e\EventState3 => 2500 Then
							
							If e\EventState2 = 1 And e\EventState3-FPSfactor < 2500 Then
								PositionEntity(Curr106\Collider, EntityX(e\room\Objects[6], True), EntityY(e\room\Objects[6], True), EntityZ(e\room\Objects[6], True))
								Contained106 = False
								ShowEntity Curr106\obj
								Curr106\Idle = False
								Curr106\State = -11
								e\EventState = 2
								Exit
							EndIf
							
							ShouldPlay = 1
							
							PositionEntity(Curr106\Collider, EntityX(e\room\Objects[5], True), (700.0 + 108.0*(Min(e\EventState3-2500.0,800)/320.0))*RoomScale , EntityZ(e\room\Objects[5], True))
							HideEntity Curr106\obj2
							
							;PointEntity(Curr106\Collider, Camera)
							RotateEntity(Curr106\Collider,0, EntityYaw(e\room\Objects[5],True)+180.0, 0, True)
							Curr106\State = -11
							Animate2(Curr106\obj, AnimTime(Curr106\obj), 206, 250, 0.1)
							Curr106\Idle = True	
							
							If e\EventState3-FPSfactor < 2500 Then 
								d.Decals = CreateDecal(0, EntityX(e\room\Objects[5], True), 936.0*RoomScale, EntityZ(e\room\Objects[5], True), 90, 0, Rnd(360)) 
								d\Timer = 90000
								d\Alpha = 0.01 : d\AlphaChange = 0.005
								d\Size = 0.1 : d\SizeChange = 0.003	
								
								If e\SoundCHN2 <> 0 Then
									If ChannelPlaying(e\SoundCHN2) Then StopChannel e\SoundCHN2
								EndIf 
								TempSound = LoadSound("SFX\LureSubject8.ogg")
								PlaySound (TempSound)
							ElseIf e\EventState3-FPSfactor < 2900 And e\EventState3 => 2900 Then
								If FemurBreakerSFX <> 0 Then FreeSound FemurBreakerSFX
								
								d.Decals = CreateDecal(0, EntityX(e\room\Objects[7], True), EntityY(e\room\Objects[7], True) , EntityZ(e\room\Objects[7], True), 0, 0, 0) 
								RotateEntity(d\obj, EntityPitch(e\room\Objects[7], True)+Rand(10,20), EntityYaw(e\room\Objects[7], True)+30, EntityRoll(d\obj))
								MoveEntity d\obj, 0,0,0.15
								RotateEntity(d\obj, EntityPitch(e\room\Objects[7], True), EntityYaw(e\room\Objects[7], True), EntityRoll(d\obj))
								
								EntityParent d\obj, e\room\Objects[7]
								;TurnEntity (d\obj, 0, 180, 0)
								
								d\Timer = 90000
								d\Alpha = 0.01 : d\AlphaChange = 0.005
								d\Size = 0.05 : d\SizeChange = 0.002
							ElseIf e\EventState3 > 3200 Then
								PositionEntity e\room\Objects[8], 0, 1000.0, 0, True 
								PositionEntity e\room\Objects[7], 0, 1000.0, 0, True 
								
								If e\EventState2 = True Then ;magneetit p‰‰ll‰ -> 106 napattu
									Contained106 = True
								Else ;magneetit pois p‰‰lt‰ -> 106 tulee ulos ja hyˆkk‰‰
									PositionEntity(Curr106\Collider, EntityX(e\room\Objects[6], True), EntityY(e\room\Objects[6], True), EntityZ(e\room\Objects[6], True))
									
									Contained106 = False
									ShowEntity Curr106\obj
									Curr106\Idle = False
									Curr106\State = -11
									
									e\EventState = 2
									Exit
								EndIf
							EndIf
							
						EndIf 
						
					EndIf
					
					If e\EventState2 Then
						PositionEntity (e\room\Objects[6],EntityX(e\room\Objects[6],True),CurveValue(-980.0*RoomScale + Sin(Float(MilliSecs())*0.04)*0.07,EntityY(e\room\Objects[6],True),200.0),EntityZ(e\room\Objects[6],True),True)
						RotateEntity(e\room\Objects[6], Sin(Float(MilliSecs())*0.03), EntityYaw(e\room\Objects[6],True), -Sin(Float(MilliSecs())*0.025), True)
					Else
						PositionEntity (e\room\Objects[6],EntityX(e\room\Objects[6],True),CurveValue(-1280.0*RoomScale,EntityY(e\room\Objects[6],True),200.0),EntityZ(e\room\Objects[6],True),True)
						RotateEntity(e\room\Objects[6], 0, EntityYaw(e\room\Objects[6],True), 0, True)
					EndIf
				EndIf
			Case "room2offices2"
				If PlayerRoom = e\room Then
					If BlinkTimer<-8 And BlinkTimer >-12 Then
						temp = Rand(1,4)
						PositionEntity e\room\Objects[0], EntityX(e\room\Objects[temp],True),EntityY(e\room\Objects[temp],True),EntityZ(e\room\Objects[temp],True),True
						RotateEntity e\room\Objects[0], 0, Rnd(360), 0
					EndIf
				EndIf
			Case "room3pit"
				If PlayerRoom = e\room Then
					If e\room\Objects[2] = 0 Then
						e\room\Objects[2] =	LoadMesh("GFX\npcs\duck_low_res.b3d")
						ScaleEntity(e\room\Objects[2], 0.07, 0.07, 0.07)
						tex = LoadTexture("GFX\npcs\duck1.png")
						EntityTexture e\room\Objects[2], tex
						PositionEntity (e\room\Objects[2], EntityX(e\room\Objects[0],True), EntityY(e\room\Objects[0],True), EntityZ(e\room\Objects[0],True))
						PointEntity e\room\Objects[2], e\room\obj
						RotateEntity(e\room\Objects[2], 0, EntityYaw(e\room\Objects[2],True),0, True)
						
						e\Sound = LoadSound("SFX\sax.ogg")
					Else
						If EntityInView(e\room\Objects[2],Camera)=False Then
							e\EventState = e\EventState + FPSfactor
							If Rand(200)=1 And e\EventState > 300 Then
								e\EventState = 0
								e\SoundCHN = PlaySound2(e\Sound, Camera, e\room\Objects[2],6.0)
							EndIf
						Else
							If e\SoundCHN <> 0 Then
								If ChannelPlaying(e\SoundCHN) Then StopChannel e\SoundCHN
							EndIf
						EndIf						
					EndIf
					
					
				EndIf						
			Case "room2closets"
				If PlayerRoom = e\room And e\EventState=0 Then
					
					e\room\NPC[0] = CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[0],True),EntityY(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True))
					e\room\NPC[0]\Sound=LoadSound("SFX\intro\scientist1.ogg")
					PlaySound2(e\room\NPC[0]\Sound, Camera, e\room\NPC[0]\Collider, 12)
					
					e\room\NPC[1] = CreateNPC(NPCtypeD, EntityX(e\room\Objects[1],True),EntityY(e\room\Objects[1],True),EntityZ(e\room\Objects[1],True))
					e\room\NPC[1]\Sound=LoadSound("SFX\intro\scientist2.ogg")
					
					tex = LoadTexture("GFX\npcs\scientist.jpg")
					EntityTexture e\room\NPC[1]\obj, tex
					PointEntity e\room\NPC[0]\Collider, e\room\NPC[1]\Collider
					PointEntity e\room\NPC[1]\Collider, e\room\NPC[0]\Collider
					
					e\EventState=1
				EndIf
				If e\EventState>0 Then
					e\EventState=e\EventState+FPSfactor
					If e\EventState < 70*3.5 Then
						RotateEntity(e\room\NPC[1]\Collider,0,CurveAngle(e\room\angle+90,EntityYaw(e\room\NPC[1]\Collider),100.0),0,True)
						
						e\room\NPC[0]\State=5
						e\room\NPC[0]\EnemyX=EntityX(e\room\NPC[1]\Collider,True)
						e\room\NPC[0]\EnemyZ=EntityZ(e\room\NPC[1]\Collider,True)
						e\room\NPC[0]\EnemyY=EntityY(e\room\NPC[1]\Collider,True)
						If e\EventState > 70*3.2 And e\EventState-FPSfactor =< 70*3.2 Then PlaySound2(IntroSFX(15),Camera,e\room\obj,15.0)
					ElseIf e\EventState < 70*6.5
						If e\EventState-FPSfactor < 70*3.5 Then
							PlaySound2(e\room\NPC[1]\Sound, Camera, e\room\NPC[1]\Collider,12.0)
						EndIf
						
						If e\EventState > 70*4.5 Then
							e\room\NPC[0]\State=8
							PointEntity e\room\NPC[0]\obj, e\room\obj
							RotateEntity(e\room\NPC[0]\Collider,0,CurveAngle(EntityYaw(e\room\NPC[0]\obj),EntityYaw(e\room\NPC[0]\Collider),30.0),0,True)
						EndIf
						PointEntity e\room\NPC[1]\obj, e\room\obj
						TurnEntity e\room\NPC[1]\obj, 0, Sin(e\EventState)*25, 0
						RotateEntity(e\room\NPC[1]\Collider,0,CurveAngle(EntityYaw(e\room\NPC[1]\obj),EntityYaw(e\room\NPC[1]\Collider),30.0),0,True)
					Else
						If e\EventState-FPSfactor < 70*6.5 Then 
							PlaySound (HorrorSFX(0))
							PlaySound (LightSFX)
						EndIf
						BlinkTimer = Max((70*6.5-e\EventState)/5.0 - Rnd(0.0,2.0),-10)
						If BlinkTimer =-10 Then
							If e\EventState > 70*7.5 And e\EventState-FPSfactor =< 70*7.5 Then PlaySound2(DamageSFX(0),Camera,e\room\NPC[0]\Collider,8.0)
							If e\EventState > 70*8.0 And e\EventState-FPSfactor =< 70*8.0 Then PlaySound2(DamageSFX(1),Camera,e\room\NPC[1]\Collider,8.0)
							SetAnimTime e\room\NPC[0]\obj, 28
							e\room\NPC[0]\State=8
							
							SetAnimTime e\room\NPC[1]\obj, 19
							e\room\NPC[1]\State = 2
						EndIf
						
						If e\EventState > 70*8.5 Then
							PositionEntity Curr173\Collider, (EntityX(e\room\Objects[0],True)+EntityX(e\room\Objects[1],True))/2,EntityY(e\room\Objects[0],True),(EntityZ(e\room\Objects[0],True)+EntityZ(e\room\Objects[1],True))/2
							PointEntity Curr173\Collider, Collider
							ResetEntity Curr173\Collider
							Delete e
						EndIf
					EndIf
				EndIf
			Case "room2offices3"
				If PlayerRoom = e\room Then
					e\EventState = e\EventState+FPSfactor
					If e\EventState > 700 Then
						If EntityDistance(e\room\RoomDoors[0]\obj, Collider)>0.5 Then 
							If EntityInView(e\room\RoomDoors[0]\obj, Camera)=False Then
								e\room\RoomDoors[0]\open = False
								e\room\RoomDoors[0]\openstate = 0
								Delete e
							EndIf
						EndIf
					EndIf
				EndIf
			Case "room2servers"
				If PlayerRoom = e\room Then
					If e\EventState=0 Then ;close the doors when the player enters the room
						UseDoor(e\room\RoomDoors[0],False)
						e\room\RoomDoors[0]\locked = True
						UseDoor(e\room\RoomDoors[1],False)
						e\room\RoomDoors[1]\locked = True
						
						If Curr096=Null Then
							Curr096 = CreateNPC(NPCtype096, EntityX(e\room\Objects[6],True),EntityY(e\room\Objects[6],True),EntityZ(e\room\Objects[6],True))
							RotateEntity Curr096\Collider, 0, e\room\angle+90, 0, True
							Curr096\State=2
							Curr096\State2=70*10
							e\EventState = 1
						Else
							PositionEntity Curr096\Collider, EntityX(e\room\Objects[6],True),EntityY(e\room\Objects[6],True),EntityZ(e\room\Objects[6],True),True
						EndIf
						
						e\Sound = LoadSound("SFX\096guard1.ogg")
						e\SoundCHN = PlaySound (e\Sound)
						
						e\room\NPC[0]=CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[7],True),EntityY(e\room\Objects[7],True),EntityZ(e\room\Objects[7],True))
						
						e\EventState=1
					ElseIf e\EventState < 70*45
						e\EventState=Min(e\EventState+FPSfactor,70*43) 
						
						If e\EventState>70*22 And Curr096\State<4 Then
							Curr096\State = 4
							If Curr096\SoundChn<>0 Then StopChannel Curr096\SoundChn
							If Curr096\Sound<> 0 Then
								FreeSound Curr096\Sound
								Curr096\Sound = 0	
							EndIf
						EndIf
						
						If e\room\NPC[0]<>Null Then
							If e\EventState < 70*3 Then
								e\room\NPC[0]\State=8
								SetAnimTime e\room\NPC[0]\obj, 115
								PointEntity e\room\NPC[0]\Collider, Curr096\Collider								
							ElseIf e\EventState-FPSfactor =< 70*15 Then ;walk to the doorway
								If e\EventState > 70*15 Then
									e\room\NPC[0]\State=3
									e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0], EntityX(e\room\Objects[8],True),0.5,EntityZ(e\room\Objects[8],True))
									e\room\NPC[0]\PathTimer=300
									DebugLog "pathstatus = " + e\room\NPC[0]\PathStatus
								EndIf
							ElseIf e\EventState<70*20 Then
								If e\room\NPC[0]\PathStatus=0 Then  
									e\room\NPC[0]\State=7
									;SetAnimTime e\room\NPC[0]\obj, 115
									PointEntity e\room\NPC[0]\obj, Curr096\Collider
									RotateEntity (e\room\NPC[0]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[0]\obj),EntityYaw(e\room\NPC[0]\Collider),30),0)
									
								EndIf
							Else ;start walking away
								If Curr096\State = 4 Then ;shoot at 096 when it starts attacking
									e\room\NPC[0]\State = 2
									PointEntity e\room\NPC[0]\obj, Curr096\Collider
									RotateEntity (e\room\NPC[0]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[0]\obj),EntityYaw(e\room\NPC[0]\Collider),30),0)
								Else
									If e\room\NPC[0]\State=7 Then
										e\room\NPC[0]\State=3
										e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0], EntityX(e\room\obj,True),0.4,EntityZ(e\room\obj,True))
										e\room\NPC[0]\PathTimer=300
										DebugLog "pathstatus = " + e\room\NPC[0]\PathStatus
									EndIf
								EndIf
								
							EndIf
							
							Curr096\Target = e\room\NPC[0]
							If EntityDistance(Curr096\Collider, e\room\NPC[0]\Collider)<0.9 Then
								e\Sound=LoadSound("SFX\096guard2.ogg")
								e\SoundCHN=PlaySound(e\Sound)
								
								Curr096\CurrSpeed = CurveValue(0, Curr096\CurrSpeed, 15.0)
								RotateEntity Curr096\Collider, CurveAngle(40, EntityPitch(Curr096\Collider),4.0), CurveAngle(e\room\angle-45, EntityYaw(Curr096\Collider),8.0), 0, True
								Animate2(e\room\NPC[0]\obj, AnimTime(e\room\NPC[0]\obj), 10, 28, 0.5, False)
								
								If AnimTime(e\room\NPC[0]\obj)>27 Then
									For i = 0 To 6
										If e\room\angle = 0 Or e\room\angle = 180 Then
											de.Decals = CreateDecal(Rand(2,3), e\room\x-Rnd(197,199)*Cos(e\room\angle)*RoomScale, 1.0, e\room\z+(140.0*(i-3))*RoomScale,0,e\room\angle+90,Rnd(360))
											de\size = Rnd(0.8,0.85) : de\sizechange = 0.001
											de.Decals = CreateDecal(Rand(2,3), e\room\x-Rnd(197,199)*Cos(e\room\angle)*RoomScale, 1.0, e\room\z+(140.0*(i-3))*RoomScale,0,e\room\angle-90,Rnd(360))
											de\size = Rnd(0.8,0.85) : de\sizechange = 0.001
										Else
											de.Decals = CreateDecal(Rand(2,3), e\room\x+(140.0*(i-3))*RoomScale, 1.0, e\room\z-Rnd(197,199)*Sin(e\room\angle)*RoomScale-Rnd(0.001,0.003),0,e\room\angle+90,Rnd(360))
											de\size = Rnd(0.8,0.85) : de\sizechange = 0.001
											de.Decals = CreateDecal(Rand(2,3), e\room\x+(140.0*(i-3))*RoomScale, 1.0, e\room\z-Rnd(197,199)*Sin(e\room\angle)*RoomScale-Rnd(0.001,0.003),0,e\room\angle-90,Rnd(360))
											de\size = Rnd(0.8,0.85) : de\sizechange = 0.001
										EndIf
										
										de.Decals = CreateDecal(Rand(2,3), EntityX(e\room\NPC[0]\Collider)+Rnd(-2,2),Rnd(0.001,0.003),EntityZ(e\room\NPC[0]\Collider)+Rnd(-2,2),90,Rnd(360),0)
										
										p.Particles = CreateParticle(EntityX(e\room\NPC[0]\Collider),EntityY(e\room\NPC[0]\Collider),EntityZ(e\room\NPC[0]\Collider), 5, Rnd(0.2,0.3), 0.15, 200)
										p\speed = 0.01
										p\SizeChange = 0.01
										p\A = 1.0
										p\Achange = -0.01
										RotateEntity p\pvt, Rnd(360),Rnd(360),0
									Next
									de\Size = Rnd(0.5,0.7)
									ScaleSprite(de\obj, de\Size,de\Size)
									
									Curr096\State=5
									
									RemoveNPC(e\room\NPC[0])
									e\room\NPC[0]=Null
								EndIf					
							EndIf
						Else
							If e\EventState >= 70*40 And e\EventState-FPSfactor < 70*40 Then ;open them again to let the player in
								e\room\RoomDoors[0]\locked=False
								e\room\RoomDoors[1]\locked=False
								UseDoor(e\room\RoomDoors[0],False)
								UseDoor(e\room\RoomDoors[1],False)
								e\room\RoomDoors[0]\locked=True
								e\room\RoomDoors[1]\locked=True
							EndIf
							
							If (e\room\angle = 0 Or e\room\angle = 180) Then ;lock the player inside
								If Abs(EntityX(Collider)-EntityX(e\room\obj,True))> 1.3 Then 
									e\EventState = 70*50
									e\Sound=0
								EndIf
							Else
								If Abs(EntityZ(Collider)-EntityZ(e\room\obj,True))> 1.3 Then 
									e\EventState = 70*50
									e\Sound=0
								EndIf
							EndIf							
						EndIf
						
					Else
						temp = UpdateLever(e\room\Objects[1]) ;power switch
						x = UpdateLever(e\room\Objects[3]) ;fuel pump
						z = UpdateLever(e\room\Objects[5]) ;generator
						
						;fuel pump on
						If x Then
							e\EventState2 = Min(1.0, e\EventState2+FPSfactor/350)
							
							;generator on
							If z Then
								If e\Sound2=0 Then e\Sound2=LoadSound("SFX\generator.ogg")
								e\EventState3 = Min(1.0, e\EventState3+FPSfactor/450)
							Else
								e\EventState3 = Min(0.0, e\EventState3-FPSfactor/450)
							EndIf
						Else
							e\EventState2 = Max(0, e\EventState2-FPSfactor/350)
							e\EventState3 = Max(0, e\EventState3-FPSfactor/450)
						EndIf
						
						If e\EventState2>0 Then e\SoundCHN=LoopSound2(RoomAmbience[8], e\SoundCHN, Camera, e\room\Objects[3], 5.0, e\EventState2*0.8)
						If e\EventState3>0 Then e\SoundCHN2=LoopSound2(e\Sound2, e\SoundCHN2, Camera, e\room\Objects[5], 6.0, e\EventState3)
						
						If temp=0 And x And z Then
							e\room\RoomDoors[0]\locked = False
							e\room\RoomDoors[1]\locked = False
							DebugLog "not locked"
						Else
							If e\room\RoomDoors[0]\open Then 
								e\room\RoomDoors[0]\locked = False
								UseDoor(e\room\RoomDoors[0],False) 
							EndIf
							If e\room\RoomDoors[1]\open Then 
								e\room\RoomDoors[1]\locked = False
								UseDoor(e\room\RoomDoors[1],False)
							EndIf
							e\room\RoomDoors[0]\locked=True
							e\room\RoomDoors[1]\locked=True							
						EndIf 
					EndIf
				EndIf
				
			Case "room2ccont"
				If PlayerRoom = e\room Then
				
					EntityPick(Camera, 1.5)
					
					For i = 1 To 5 Step 2
						If PickedEntity() = e\room\Objects[i] Then
							DrawHandIcon = True
							If MouseHit1 Then GrabbedEntity = e\room\Objects[i]
							
							If e\EventState = 0 Then 
								If i = 3 Then 
									e\EventState = Max(e\EventState,1)
									PlaySound HorrorSFX(7)
									PlaySound LeverSFX
								EndIf
							EndIf 
						End If
						
						Local prevpitch# = EntityPitch(e\room\Objects[i])
						
						If MouseDown1 Or MouseHit1 Then
							If GrabbedEntity <> 0 Then
								If GrabbedEntity = e\room\Objects[i] Then
									DrawHandIcon = True
									TurnEntity(e\room\Objects[i], mouse_y_speed_1 * 2.5, 0, 0)
									RotateEntity(GrabbedEntity, Max(Min(EntityPitch(e\room\Objects[i]), 85), -85), EntityYaw(e\room\Objects[i]), 0)
									
									DrawArrowIcon(0) = True
									DrawArrowIcon(2) = True
									
								EndIf
							EndIf
						Else
							If EntityPitch(e\room\Objects[i]) > 0 Then
								RotateEntity(e\room\Objects[i], CurveValue(85, EntityPitch(e\room\Objects[i]), 10), EntityYaw(e\room\Objects[i]), 0)
							Else
								RotateEntity(e\room\Objects[i], CurveValue(-85, EntityPitch(e\room\Objects[i]), 10), EntityYaw(e\room\Objects[i]), 0)
							EndIf
							GrabbedEntity = 0
						End If
						
						If EntityPitch(e\room\Objects[i]) > 83 Then
							If prevpitch =< 83 Then PlaySound2(LeverSFX, Camera, e\room\Objects[i])
							If i = 3 Then 
								SecondaryLightOn = CurveValue(1.0, SecondaryLightOn, 10.0)
								If prevpitch =< 83 Then
									PlaySound2(LightSFX, Camera, e\room\Objects[i])
								EndIf		
							Else
								RemoteDoorOn = True
							EndIf
						ElseIf EntityPitch(e\room\Objects[i]) < -83
							
							If prevpitch => -83 Then PlaySound2(LeverSFX, Camera, e\room\Objects[i])
							If i = 3 Then 
								If prevpitch => -83 Then
									PlaySound2(LightSFX, Camera, e\room\Objects[i])
									For r.Rooms = Each Rooms
										For z = 0 To 19
											If r\LightSprites[z] <> 0 Then HideEntity r\LightSprites[z]
										Next 
									Next 
								EndIf 
								SecondaryLightOn = CurveValue(0.0, SecondaryLightOn, 10.0)
							Else
								RemoteDoorOn = False
							EndIf						
						EndIf
					Next
					
					If e\EventState > 0 And e\EventState < 200 Then
						e\EventState = e\EventState + FPSfactor
						RotateEntity(e\room\Objects[3], CurveValue(-85, EntityPitch(e\room\Objects[3]), 5), EntityYaw(e\room\Objects[3]), 0)
					EndIf 
					
				EndIf
				
			Case "forest"
				If PlayerRoom = e\room Then	
					If e\EventState = 0 Then
						e\room\Objects[0]=LoadAnimMesh("GFX\map\forestterrain.b3d")
						ScaleEntity e\room\Objects[0], RoomScale, RoomScale, RoomScale
						PositionEntity e\room\Objects[0], e\room\x, 0, e\room\z
						EntityType e\room\Objects[0], HIT_MAP
						EntityPickMode e\room\Objects[0], 3
						
						For c=1 To CountChildren(e\room\Objects[0])
							Local node=GetChild(e\room\Objects[0],c)	
							EntityAutoFade node, 2.0, 6.0
							;EntityFX node, 8+32
							;EntityBlend node, 2	
							;Local classname$=Lower(KeyValue(node,"classname"))
							;If classname = "background" Then
								;HideEntity node
								;EntityBlend node, 2
								;EntityOrder node, 10
							;EndIf
						Next
						
						;EntityFX e\room\Objects[0], 8
						
						
						
						
						e\EventState = 1.0
					Else
						CameraFogRange Camera, 2, 5
						CameraFogColor (Camera,130,171,196)
						CameraClsColor (Camera,130,171,196)
						CameraRange(Camera, 0.05, 10)						
					EndIf
				EndIf
				
			Case "914"
				If PlayerRoom = e\room Then
					Achievements(Achv914) = True
					
					EntityPick(Camera, 1.0)
					If PickedEntity() = e\room\Objects[0] Then
						DrawHandIcon = True
						If MouseHit1 Then GrabbedEntity = e\room\Objects[0]
					ElseIf PickedEntity() = e\room\Objects[1]
						DrawHandIcon = True
						If MouseHit1 Then GrabbedEntity = e\room\Objects[1]
					EndIf
					
					If MouseDown1 Or MouseHit1 Then
						If GrabbedEntity <> 0 Then ;avain
							If GrabbedEntity = e\room\Objects[0] Then
								If e\EventState = 0 Then
									DrawHandIcon = True
									TurnEntity(GrabbedEntity, 0, 0, -mouse_x_speed_1 * 2.5)
									
									angle = WrapAngle(EntityRoll(e\room\Objects[0]))
									If angle > 181 Then DrawArrowIcon(3) = True
									DrawArrowIcon(1) = True
									
									If angle < 90 Then
										RotateEntity(GrabbedEntity, 0, 0, 361.0)
									ElseIf angle < 180
										RotateEntity(GrabbedEntity, 0, 0, 180)
									EndIf
									
									If angle < 181 And angle > 90 Then
										For it.Items = Each Items
											If it\obj <> 0 And it\Picked = False Then
												If Abs(EntityX(it\obj) - (e\room\x - 712.0 * RoomScale)) < 200.0 Then
													If Abs(EntityY(it\obj) - (e\room\y + 648.0 * RoomScale)) < 104.0 Then
														e\EventState = 1
														e\SoundCHN = PlaySound2(MachineSFX, Camera, e\room\Objects[1])
														Exit
													EndIf
												End If
											End If
										Next
									EndIf
								End If
							ElseIf GrabbedEntity = e\room\Objects[1]
								If e\EventState = 0 Then
									DrawHandIcon = True
									TurnEntity(GrabbedEntity, 0, 0, -mouse_x_speed_1 * 2.5)
									
									angle# = WrapAngle(EntityRoll(e\room\Objects[1]))
									DrawArrowIcon(3) = True
									DrawArrowIcon(1) = True
									
									If angle > 90 Then
										If angle < 180 Then
											RotateEntity(GrabbedEntity, 0, 0, 90.0)
										ElseIf angle < 270
											RotateEntity(GrabbedEntity, 0, 0, 270)
										EndIf
									EndIf
									
								End If
							End If
						End If
					Else
						GrabbedEntity = 0
					End If
					
					Local setting$ = ""
					
					If GrabbedEntity <> e\room\Objects[1] Then
						angle# = WrapAngle(EntityRoll(e\room\Objects[1]))
						If angle < 22.5 Then
							angle = 0
							setting = "1:1"
						ElseIf angle < 67.5
							angle = 40
							setting = "coarse"
						ElseIf angle < 180
							angle = 90
							setting = "rough"
						ElseIf angle > 337.5
							angle = 359 - 360
							setting = "1:1"
						ElseIf angle > 292.5
							angle = 320 - 360
							setting = "fine"
						Else
							angle = 270 - 360
							setting = "very fine"
						End If
						RotateEntity(e\room\Objects[1], 0, 0, CurveValue(angle, EntityRoll(e\room\Objects[1]), 20))
					EndIf
					
					For i% = 0 To 1
						If GrabbedEntity = e\room\Objects[i] Then
							If Not EntityInView(e\room\Objects[i], Camera) Then
								GrabbedEntity = 0
							ElseIf EntityDistance(e\room\Objects[i], Camera) > 1.0
								GrabbedEntity = 0
							End If
						End If
					Next
					
					If e\EventState > 0 Then
						e\EventState = e\EventState + FPSfactor
						
						
						e\room\RoomDoors[1]\open = False
						If e\EventState > 70 * 2 Then
							e\room\RoomDoors[0]\open = False
						EndIf
						
						If Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\Objects[2], True), EntityZ(e\room\Objects[2], True)) < (170.0 * RoomScale) Then
							
							If setting = "rough" Or setting = "coarse" Then
								If e\EventState > 70 * 2.6 And e\EventState - FPSfactor2 < 70 * 2.6 Then PlaySound Death914SFX
							EndIf
							
							If e\EventState > 70 * 3 Then
								Select setting
									Case "rough"
										KillTimer = Min(-1, KillTimer)
										BlinkTimer = -10
										If e\SoundCHN <> 0 Then StopChannel e\SoundCHN
									Case "coarse"
										BlinkTimer = -10
										If e\EventState - FPSfactor2 < 70 * 3 Then PlaySound Use914SFX
									Case "1:1"
										BlinkTimer = -10
										If e\EventState - FPSfactor2 < 70 * 3 Then PlaySound Use914SFX
									Case "fine", "very fine"
										BlinkTimer = -10
										If e\EventState - FPSfactor2 < 70 * 3 Then PlaySound Use914SFX	
								End Select
							End If
						EndIf
						
						If e\EventState > (6 * 70) Then	
							RotateEntity(e\room\Objects[0], EntityPitch(e\room\Objects[0]), EntityYaw(e\room\Objects[0]), CurveAngle(0, EntityRoll(e\room\Objects[0]),10.0))
						Else
							RotateEntity(e\room\Objects[0], EntityPitch(e\room\Objects[0]), EntityYaw(e\room\Objects[0]), 180)
						EndIf
						
						If e\EventState > (12 * 70) Then							
							For it.Items = Each Items
								If it\obj <> 0 And it\Picked = False Then
									If Distance(EntityX(it\obj), EntityZ(it\obj), EntityX(e\room\Objects[2], True), EntityZ(e\room\Objects[2], True)) < (180.0 * RoomScale) Then
										Use914(it, setting, EntityX(e\room\Objects[3], True), EntityY(e\room\Objects[3], True), EntityZ(e\room\Objects[3], True))
										
									End If
								End If
							Next
							
							If Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\Objects[2], True), EntityZ(e\room\Objects[2], True)) < (160.0 * RoomScale) Then
								Select setting
									Case "coarse"
										Injuries = 4.0
										Msg = "You notice countless small incisions all around your body. They're bleeding heavily."
										MsgTimer = 70*8
									Case "1:1"
										InvertMouse = (Not InvertMouse)
									Case "fine", "very fine"
										SuperMan = True
								End Select
								BlurTimer = 1000
								PositionEntity(Collider, EntityX(e\room\Objects[3], True), EntityY(e\room\Objects[3], True) + 1.0, EntityZ(e\room\Objects[3], True))
								ResetEntity(Collider)
								DropSpeed = 0
							EndIf								
							
							e\room\RoomDoors[0]\open = True
							e\room\RoomDoors[1]\open = True
							RotateEntity(e\room\Objects[0], 0, 0, 0)
							e\EventState = 0
						End If
					End If
					
				EndIf
		End Select
		
	Next
	
	If ExplosionTimer > 0 Then
		ExplosionTimer = ExplosionTimer+FPSfactor
		
		If ExplosionTimer < 140.0 Then
			If ExplosionTimer-FPSfactor < 5.0 Then
				ExplosionSFX = LoadSound("SFX\nuclear1.ogg")
				PlaySound ExplosionSFX
				CameraShake = 10.0
				ExplosionTimer = 5.0
			EndIf
			
			CameraShake = CurveValue(ExplosionTimer/60.0,CameraShake, 50.0)
		Else
			CameraShake = Min((ExplosionTimer/20.0),20.0)
			If ExplosionTimer-FPSfactor < 140.0 Then
				BlinkTimer = 1.0
				ExplosionSFX = LoadSound("SFX\nuclear2.ogg")
				PlaySound ExplosionSFX				
				For i = 0 To 40
					p.Particles = CreateParticle(EntityX(Collider)+Rnd(-0.5,0.5),EntityY(Collider)-Rnd(0.2,1.5),EntityZ(Collider)+Rnd(-0.5,0.5),0, Rnd(0.2,0.6), 0.0, 350)	
					RotateEntity p\pvt,-90,0,0,True
					p\speed = Rnd(0.05,0.07)
				Next
			EndIf
			LightFlash = Min((ExplosionTimer-160.0)/40.0,2.0)
			If ExplosionTimer > 160 Then KillTimer = Min(KillTimer,-0.1) : EndingTimer = Min(KillTimer,-0.1)
			If ExplosionTimer > 500 Then ExplosionTimer = 0
		EndIf
		
	EndIf

End Function

Collisions HIT_PLAYER, HIT_MAP, 2, 2
Collisions HIT_PLAYER, HIT_PLAYER, 1, 3
Collisions HIT_ITEM, HIT_MAP, 2, 2
Collisions HIT_APACHE, HIT_APACHE, 1, 2

DrawLoading(90, True)

;----------------------------------- meshes and textures ----------------------------------------------------------------

;Global Listener%
Global FogTexture%, Fog%
Global GasMaskTexture%, GasMaskOverlay%
Global InfectTexture%, InfectOverlay%
Global DarkTexture%, Dark%
Global SignObj%
Global Collider%, Head%

Global TeslaTexture%

Global LightTexture%, Light%
Dim LightSpriteTex%(5)
Global DoorOBJ%, DoorFrameOBJ%

Global LeverOBJ%, LeverBaseOBJ%

;Dim BigDoorOBJ%(2)
Global DoorColl%
Global ButtonOBJ%, ButtonKeyOBJ%, ButtonCodeOBJ%

Dim GorePics%(10)
Dim DecalTextures%(20)

Global Monitor%, MonitorTexture%
Global CamBaseOBJ%, CamOBJ%

BumpPower 0.03

;---------------------------------------------------------------------------------------------------

Include "menu.bb"
MainMenuOpen = True

;---------------------------------------------------------------------------------------------------

FlushKeys()
FlushMouse()

DrawLoading(100, True)

LoopDelay = MilliSecs()

;----------------------------------------------------------------------------------------------------------------------------------------------------
;----------------------------------------------       		MAIN LOOP                 ---------------------------------------------------------------
;----------------------------------------------------------------------------------------------------------------------------------------------------

Repeat
	
	Cls
	
	CurTime = MilliSecs()
	ElapsedTime = (CurTime - PrevTime) / 1000.0
	PrevTime = CurTime
	FPSfactor = Max(Min(ElapsedTime * 70, 5.0), 0.2)
	FPSfactor2 = FPSfactor
	
	If ((MenuOpen Or InvOpen Or ConsoleOpen Or SelectedDoor <> Null Or SelectedScreen <> Null) And mpState = 0) Then FPSfactor = 0
	
	If Framelimit > 0 Or Framelimit < 255
	   ; Framelimit
	   Local WaitingTime% = (1000.0 / Framelimit) - (MilliSecs() - LoopDelay)
		Delay WaitingTime%
		
	   LoopDelay = MilliSecs()
	EndIf
	
	;Counting the fps
	If CheckFPS < MilliSecs() Then
		FPS = ElapsedLoops
		ElapsedLoops = 0
		CheckFPS = MilliSecs()+1000
	EndIf
	ElapsedLoops = ElapsedLoops + 1
	
	DoubleClick = False
	MouseHit1 = MouseHit(1)
	If MouseHit1 Then
		If MilliSecs() - LastMouseHit1 < 800 Then DoubleClick = True
		LastMouseHit1 = MilliSecs()
	EndIf
	
	Local prevmousedown1 = MouseDown1
	MouseDown1 = MouseDown(1)
	If prevmousedown1 = True And MouseDown1=False Then MouseUp1 = True Else MouseUp1 = False
	
	MouseHit2 = MouseHit(2)
	
	If (Not MouseDown1) And (Not MouseHit1) Then GrabbedEntity = 0
	
	UpdateMusic()
	
	If MainMenuOpen Then
		ShouldPlay = 2
		UpdateMainMenu()
	Else
		ShouldPlay = 0
		
		DrawHandIcon = False
		
		If FPSfactor > 0 Then 
			UpdateSecurityCams()
		EndIf
		
		If KeyHit(KEY_INV) Then 
			If InvOpen Then
				ResumeSounds()
			Else
				PauseSounds()
			EndIf
			InvOpen = Not InvOpen 
			SelectedItem = Null 
		EndIf
		
		If PlayerRoom\RoomTemplate\Name <> "pocketdimension" And PlayerRoom\RoomTemplate\Name <> "gatea"  Then 
			If Rand(1500) = 1 Then
				PositionEntity (SoundEmitter, EntityX(Camera) + Rnd(-1.0, 1.0), 0.0, EntityZ(Camera) + Rnd(-1.0, 1.0))
				CurrAmbientSFX = Rand(0,AmbientSFXAmount-1)
				If AmbientSFX(CurrAmbientSFX)=0 Then AmbientSFX(CurrAmbientSFX)=LoadSound("SFX\ambient\ambient"+(CurrAmbientSFX+1)+".ogg")
				AmbientSFXCHN = PlaySound2(AmbientSFX(CurrAmbientSFX), Camera, SoundEmitter)
			EndIf
		EndIf
		
		If AmbientSFX(CurrAmbientSFX)<>0 Then
			If ChannelPlaying(AmbientSFXCHN)=0 Then FreeSound AmbientSFX(CurrAmbientSFX) : AmbientSFX(CurrAmbientSFX) = 0
		EndIf
		
		If FPSfactor > 0 Then 
			LightVolume = CurveValue(TempLightVolume, LightVolume, 50.0)
			CameraFogRange(Camera, CameraFogNear*LightVolume,CameraFogFar*LightVolume)
			CameraFogColor(Camera, 0,0,0)
			CameraFogMode Camera,1
			CameraRange(Camera, 0.05, Min(CameraFogFar*LightVolume*1.5,28))
		EndIf
		
		UpdateRoomTimer = UpdateRoomTimer-FPSfactor2
		If UpdateRoomTimer =< 0 Then 
			UpdateRooms()
			UpdateRoomTimer = 15
		EndIf
		
		If (((Not MenuOpen) And (Not InvOpen) And (SelectedDoor = Null) And (ConsoleOpen = False)) Or mpState <> 0) Then	
			AmbientLight Brightness, Brightness, Brightness	
			PlayerSoundVolume = CurveValue(0.0, PlayerSoundVolume, 5.0)
			
			UpdateEmitters()
			MouseLook()
			MovePlayer()
			If mpState <> 2 Then
				;UpdateDoors()
				UpdateEvents()
				UpdateMTF()
				UpdateNPCs()
			EndIf
			UpdateDoors()
			UpdateNetwork()
			UpdatePlayers()
			UpdateDecals()
			UpdateItems()
			UpdateParticles()
			UpdateScreens()
		EndIf
		
		UpdateWorld()
		RenderWorld()
		
		BlurVolume = Min(CurveValue(0.1, BlurVolume, 20.0),0.95)
		If BlurTimer > 0.0 Then
			BlurVolume = Max(Min(0.95, BlurTimer / 1000.0), BlurVolume)
			BlurTimer = Max(BlurTimer - FPSfactor, 0.0)
		End If
		
		UpdateBlur(BlurVolume)
		
		;[Block]
		
		Local darkA# = Sin(MilliSecs()/5000)*0.08;0.0
		If (Not MenuOpen)  Then
			If Sanity < 0 Then
				Sanity = Min(Sanity + FPSfactor, 0.0)
				If Sanity < (-200) Then 
					darkA = Max(Min((-Sanity - 200) / 700.0, 0.6), darkA)
					If KillTimer => 0 Then 
						HeartBeatVolume = Min(Abs(Sanity+200)/500.0,1.0)
						HeartBeatRate = Max(70 + Abs(Sanity+200)/6.0,HeartBeatRate)
					EndIf
				EndIf
			End If
			
			If EyeStuck > 0 Then 
				BlinkTimer = BLINKFREQ
				EyeStuck = Max(EyeStuck-FPSfactor,1)
				
				If EyeStuck < 9000 Then BlurTimer = Max(BlurTimer, (9000-EyeStuck)*0.5)
				If EyeStuck < 6000 Then darkA = Min(Max(darkA, (6000-EyeStuck)/5000.0),1.0)
				If EyeStuck < 9000 And EyeStuck+FPSfactor =>9000 Then 
					Msg = "Your eyes are starting to hurt"
					MsgTimer = 70*6
				EndIf
			EndIf
			
			If BlinkTimer < 0 Then
				If BlinkTimer > - 5 Then
					darkA = Max(darkA, Sin(Abs(BlinkTimer * 18.0)))
				ElseIf BlinkTimer > - 15
					darkA = 1.0
				Else
					darkA = Max(darkA, Abs(Sin(BlinkTimer * 18.0)))
				EndIf
				
				If BlinkTimer <= - 20 Then BlinkTimer = BLINKFREQ
				BlinkTimer = BlinkTimer - FPSfactor
			Else
				BlinkTimer = BlinkTimer - FPSfactor * 0.6
				If EyeIrritation > 0 Then BlinkTimer=BlinkTimer-Min(EyeIrritation / 100.0 + 1.0, 4.0) * FPSfactor	
				If EyeSuper > 0 Then BlinkTimer=BlinkTimer+Min(EyeSuper / 1000.0, 0.3) * FPSfactor		
				
				darkA = Max(darkA, 0.0)
			End If
			
			EyeIrritation = Max(0, EyeIrritation - FPSfactor)
			EyeSuper = Max(0, EyeSuper - FPSfactor)			
			
			LightBlink = Max(LightBlink - (FPSfactor / 35.0), 0)
			If LightBlink > 0 Then darkA = Min(Max(darkA, LightBlink + (1.0 - LightBlink) * Rnd(0.3, 0.8)), 1.0)
			
			;(-0.2) - 1.8
			;min    (-0.2) - 0
			
			darkA = Max((1.0-SecondaryLightOn)*0.91, darkA)
			
			If KillTimer >= 0 Then
				
			Else
				InvOpen = False
				SelectedItem = Null
				SelectedScreen = Null
				BlurTimer = Abs(KillTimer*10)
				KillTimer=KillTimer-FPSfactor
				If KillTimer < - 360 Then 
					MenuOpen = True 
					If SelectedEnding <> "" Then EndingTimer = Min(KillTimer,-0.1)
				EndIf
				darkA = Max(darkA, Min(Abs(KillTimer / 400.0), 1.0))
			EndIf
			
			If FallTimer < 0 Then
				InvOpen = False
				SelectedItem = Null
				SelectedScreen = Null
				BlurTimer = Abs(FallTimer*10)
				FallTimer=FallTimer-FPSfactor
				darkA = Max(darkA, Min(Abs(FallTimer / 400.0), 1.0))				
			EndIf
			
			If SelectedItem <> Null Then
				If SelectedItem\itemtemplate\tempname = "navigator" Or SelectedItem\itemtemplate\tempname = "nav" Then darkA = Max(darkA, 0.5)
			End If
			If SelectedScreen <> Null Then darkA = Max(darkA, 0.5)
			
			EntityAlpha(Dark, darkA)	
		EndIf
		
		If LightFlash > 0 Then
			ShowEntity Light
			DebugLog "lightflash: "+LightFlash
			EntityAlpha(Light, Max(Min(LightFlash + Rnd(-0.2, 0.2), 1.0), 0.0))
			LightFlash = Max(LightFlash - (FPSfactor / 70.0), 0)
		Else
			HideEntity Light
			;EntityAlpha(Light, LightFlash)
		End If
		
		;[End block]
		
		If KeyHit(63) Then
			If PlayerRoom\RoomTemplate\Name = "exit1" Or PlayerRoom\RoomTemplate\Name = "173" Or PlayerRoom\RoomTemplate\Name = "gatea" Then
				Msg = "You can't save in this location"
				MsgTimer = 70 * 4
			Else
				SaveGame(SavePath + CurrSave + "\")
				Msg = "Game saved"
				MsgTimer = 70 * 4
			EndIf
		End If
		
		If KeyHit(61) Then 
			ConsoleOpen = (Not ConsoleOpen)
			FlushKeys()
		EndIf
		
		DrawGUI()
		DrawMenu()
		
		If EndingTimer < 0 Then
			If SelectedEnding <> "" Then DrawEnding()
		EndIf
		
		UpdateConsole()
		
		If MsgTimer > 0 Then
			Color 0,0,0
			Text((GraphicWidth / 2)+1, (GraphicHeight / 2) + 201, Msg, True) 			
			Color Min(MsgTimer / 2, 255), Min(MsgTimer / 2, 255), Min(MsgTimer / 2, 255)
			Text((GraphicWidth / 2), (GraphicHeight / 2) + 200, Msg, True) 
			MsgTimer=MsgTimer-FPSfactor2 
		End If
		
		Color 255, 255, 255
		If ShowFPS Then Text 20, 20, "FPS: " + FPS
		
	End If
	
	Flip 
	
Forever

;----------------------------------------------------------------------------------------------------------------------------------------------------
;----------------------------------------------------------------------------------------------------------------------------------------------------
;----------------------------------------------------------------------------------------------------------------------------------------------------
Function UpdatePlayers()
	If mpState = 0 Then Return
	
	For i% = 0 To MpNumClients - 1
		If i% <> MpMyID Then
			x# = MpClients(i%)\ft*((MpClients(i%)\x# - MpClients(i%)\lx#)/(MpClients(i%)\lft)) + MpClients(i%)\lx#
			y# = MpClients(i%)\ft*((MpClients(i%)\y# - MpClients(i%)\ly#)/(MpClients(i%)\lft)) + MpClients(i%)\ly#
			z# = MpClients(i%)\ft*((MpClients(i%)\z# - MpClients(i%)\lz#)/(MpClients(i%)\lft)) + MpClients(i%)\lz#
			
			temp# = (MpClients(i%)\yaw# - MpClients(i%)\lyaw#)
			
			If temp# > 180 Then
				temp# = temp# - 360
			EndIf
			If temp# < -180 Then
				temp# = temp# + 360
			EndIf
			
			yaw# = MpClients(i%)\ft*(temp#/(MpClients(i%)\lft)) + MpClients(i%)\lyaw#	
			
			currspeedT# = Sqr((((MpClients(i%)\x# - MpClients(i%)\lx#)*(MpClients(i%)\x# - MpClients(i%)\lx#))+((MpClients(i%)\z# - MpClients(i%)\lz#)*(MpClients(i%)\z# - MpClients(i%)\lz#))) / MpClients(i%)\lft%)
			
			deg# = yaw# - (ATan2(MpClients(i%)\z# - MpClients(i%)\lz#, MpClients(i%)\x# - MpClients(i%)\lx#));
			
			If deg# >  180 Then deg# = deg# - 360.0
			If deg# < -180 Then deg# = deg# + 360.0
			
			dir% = 0
			
			If deg < -90.0 - 45.0 Then
				dir% = 3
			ElseIf deg < -90.0 + 45.0 Then
				dir% = 0
			ElseIf deg < 45.0 Then
				dir% = 1
			ElseIf deg < 90.0 + 45.0 Then
				dir% = 2
			Else
				dir% = 3
			EndIf
			
			;DebugLog "dir: "+dir
			;DebugLog "deg: "+deg
			;DebugLog "yaw: "+yaw
			;DebugLog "deg - yaw: "+(deg-yaw)
			DebugLog "currSpeedT: "+currspeedT
			
			prevframe = AnimTime(MpClients(i%)\obj)
			
			If currspeedT <= 0.005 Then
				Animate2(MpClients(i%)\obj, AnimTime(MpClients(i%)\obj), 210, 235, 0.1)
			Else
				If dir = 0 Then
					Animate2(MpClients(i%)\obj, AnimTime(MpClients(i%)\obj), 236, 260, MpAnimSpeed# * currspeedT#)
				ElseIf dir = 1 Then
					Animate2(MpClients(i%)\obj, AnimTime(MpClients(i%)\obj), 281, 300, MpAnimSpeed# * currspeedT#)
				ElseIf dir = 2 Then
					Animate2(MpClients(i%)\obj, AnimTime(MpClients(i%)\obj), 260, 236, -(MpAnimSpeed# * currspeedT#))
				Else
					Animate2(MpClients(i%)\obj, AnimTime(MpClients(i%)\obj), 261, 280, MpAnimSpeed# * currspeedT#)
				EndIf
			EndIf
			
			If currspeedT > 0.001 Then
				If prevframe < 244 And AnimTime(MpClients(i%)\obj)=>244 Then
					PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, MpClients(i%)\Collider, 8.0, Rnd(0.3,0.5))						
				ElseIf prevframe < 256 And AnimTime(MpClients(i%)\obj)=>256
					PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, MpClients(i%)\Collider, 8.0, Rnd(0.3,0.5))
				EndIf
			EndIf
			
			PositionEntity(MpClients(i%)\Collider, x#, y#, z#)
			PositionEntity(MpClients(i%)\obj, x#, y - 0.32, z#)
			RotateEntity(MpClients(i%)\obj,0,yaw# - 180.0 ,0)
			MpClients(i%)\ft = MpClients(i%)\ft + 1
			;DebugLog "Update: "+x+", "+y+", "+z+", "+yaw+" : "+(MpClients(i%)\ft)+" from "+mpState+"|"+i+", my: "+MpMyID
		EndIf
	Next
End Function

Function MakeOldPos(x.Client)
	x\lx# = x\x#
	x\ly# = x\y#
	x\lz# = x\z#
	x\lyaw# = x\yaw#
	x\lft% = x\ft%
End Function

Function UpdateNetwork()
	If mpState = 0 Then Return
	
	If mpState = 1 Then
		For i% = 1 To MpNumClients - 1
			While ReadAvail(MpClients(i%)\stream)
				msgR$ = ReadLine(MpClients(i%)\stream)
				Select msgR$
					Case "move"
						x# = ReadLine(MpClients(i%)\stream)
						y# = ReadLine(MpClients(i%)\stream)
						z# = ReadLine(MpClients(i%)\stream)
						yaw# = ReadLine(MpClients(i%)\stream)
						
						MakeOldPos(MpClients(i%))
						MpClients(i%)\x# = x#
						MpClients(i%)\y# = y#
						MpClients(i%)\z# = z#
						MpClients(i%)\yaw# = yaw#
						MpClients(i%)\ft% = 0
						
						For j% = 1 To MpNumClients - 1
							If j% <> i% Then
								WriteLine MpClients(j%)\stream,"move"
								WriteLine MpClients(j%)\stream,i%
								WriteLine MpClients(j%)\stream,x#
								WriteLine MpClients(j%)\stream,y#
								WriteLine MpClients(j%)\stream,z#
								WriteLine MpClients(j%)\stream,yaw#
								WriteLine MpClients(j%)\stream,Crouch
							EndIf
						Next
						;DebugLog "Receieved: "+x#+","+y#+","+z#+"|TIME: "+BlinkTimer
					Case "doorupdate"
						offset% = ReadLine(MpClients(i%)\stream)
						offset2% = 0
						For d.Doors = Each Doors
							If offset2 = offset
								UseDoor(d, True, False)
							EndIf
							offset2 = offset2+1
						Next
						For j% = 1 To MpNumClients - 1
							If j <> i
								WriteLine MpClients(j%)\stream, "doorupdate"
								WriteLine MpClients(j%)\stream, offset
							EndIf
						Next
					Case "itemhide"
						offset% = ReadLine(MpClients(i%)\stream)
						offset2% = 0
						For it.Items = Each Items
							If offset2 = offset
								HideEntity(it\obj)
								it\picked = True
							EndIf
							offset2 = offset2+1
						Next
						For j% = 1 To MpNumClients - 1
							If j <> i
								WriteLine MpClients(j%)\stream, "itemhide"
								WriteLine MpClients(j%)\stream, offset
							EndIf
						Next
					Case "itemshow"
						offset% = ReadLine(MpClients(i%)\stream)
						x = ReadLine(MpClients(i%)\stream)
						y = ReadLine(MpClients(i%)\stream)
						z = ReadLine(MpClients(i%)\stream)
						offset2% = 0
						For it.Items = Each Items
							If offset2 = offset
								ShowEntity(it\obj)
								PositionEntity(it\obj, x, y, z, True)
								ResetEntity(it\obj)
								it\picked = False
								Exit
							EndIf
							offset2 = offset2+1
						Next
						For j% = 1 To MpNumClients - 1
							If j <> i
								WriteLine MpClients(j%)\stream, "itemshow"
								WriteLine MpClients(j%)\stream, offset
								WriteLine MpClients(j%)\stream, x
								WriteLine MpClients(j%)\stream, y
								WriteLine MpClients(j%)\stream, z
							EndIf
						Next
					Case "removeitem"
						offset% = ReadLine(MpClients(i%)\stream)
						offset2% = 0
						For it.Items = Each Items
							If offset2 = offset
								RemoveItem(it, False)
							EndIf
							offset2 = offset2+1
						Next
						For j% = 1 To MpNumClients - 1
							If j <> i
								WriteLine MpClients(j%)\stream, "removeitem"
								WriteLine MpClients(j%)\stream, offset
							EndIf
						Next
						
						
				End Select
				;DebugLog "msg: "+msg$
			Wend
		Next
	EndIf
	
	If mpState = 2 Then
		While ReadAvail(MpStream)
			msgR$ = ReadLine(MpStream)
			Select msgR$
				Case "move"
					i% = ReadLine(MpStream)
					x# = ReadLine(MpStream)
					y# = ReadLine(MpStream)
					z# = ReadLine(MpStream)
					yaw# = ReadLine(MpStream)
					Crouch = ReadLine(MpStream)
					
					MakeOldPos(MpClients(i%))
					MpClients(i%)\x# = x#
					MpClients(i%)\y# = y#
					MpClients(i%)\z# = z#
					MpClients(i%)\yaw# = yaw#
					MpClients(i%)\ft% = 0
					MpClients(i%)\Crouch = Crouch
					;DebugLog "Receieved: "+x#+","+y#+","+z#+"|TIME: "+BlinkTimer
				Case "doorupdate"
					offset% = ReadLine(MpStream)
					offset2% = 0
					For d.Doors = Each Doors
						If offset2 = offset
							UseDoor(d, True, False)
						EndIf
						offset2 = offset2+1
					Next
				Case "itemhide"
					offset% = ReadLine(MpStream)
					offset2% = 0
					For it.Items = Each Items
						If offset2 = offset
							HideEntity(it\obj)
							it\picked = True
						EndIf
						offset2 = offset2+1
					Next
				Case "itemshow"
					offset% = ReadLine(MpStream)
					x = ReadLine(MpStream)
					y = ReadLine(MpStream)
					z = ReadLine(MpStream)
					offset2% = 0
					
					For it.Items = Each Items
						If offset2 = offset
							ShowEntity(it\obj)
							PositionEntity(it\obj, x, y, z, True)
							ResetEntity(it\obj)
							it\picked = False
							Exit
						EndIf
						offset2 = offset2+1
					Next
				Case "removeitem"
					offset% = ReadLine(MpStream)
					offset2% = 0
					For it.Items = Each Items
						If offset2 = offset
							RemoveItem(it, False)
						EndIf
						offset2 = offset2+1
					Next
					
					
			End Select
			;DebugLog "msg: "+msg$
		Wend
	EndIf
	
End Function

Function Kill()
	If GodMode Then Return
	
	If KillTimer >= 0 Then
		KillAnim = Rand(0,1)
		PlaySound(DeathSFX(0))
		If SelectedMode = 1 Then
			DeleteDir(SavePath + CurrSave + "\")
			LoadSaveGames()
		End If
		
		KillTimer = Min(-1, KillTimer)
		ShowEntity Head
		PositionEntity(Head, EntityX(Camera, True), EntityY(Camera, True), EntityZ(Camera, True), True)
		ResetEntity (Head)
		RotateEntity(Head, 0, EntityYaw(Camera), 0)		
	EndIf
End Function

Function DrawEnding()
	
	ShowPointer()
	
	FPSfactor = 0
	EndingTimer=EndingTimer-FPSfactor2
	
	Local x,y,width,height, temp
	Local itt.ItemTemplates, r.Rooms
	
	Select Lower(SelectedEnding)
		Case "b2", "a1"
			ClsColor Max(255+(EndingTimer)*2.8,0), Max(255+(EndingTimer)*2.8,0), Max(255+(EndingTimer)*2.8,0)
		Default
			ClsColor 0,0,0
	End Select
	
	ShouldPlay = 0
	
	Cls
	
	If EndingTimer<-200 Then
		If EndingScreen = 0 Then 
			EndingScreen = LoadImage("GFX\endingscreen.pt")
			
			temp = LoadSound ("SFX\Ending.ogg")
			PlaySound temp
			
			PlaySound LightSFX
			;playsound pam
		EndIf
		
		If EndingTimer > -700 Then 
			
			;-200 -> -700
			;Max(50 - (Abs(KillTimer)-200),0)    =    0->50
			If Rand(1,150)<Min((Abs(EndingTimer)-200),155) Then
				DrawImage EndingScreen, GraphicWidth/2-400, GraphicHeight/2-400
			Else
				Color 0,0,0
				Rect 100,100,GraphicWidth-200,GraphicHeight-200
				Color 255,255,255
			EndIf
			
			If EndingTimer+FPSfactor2 > -450 And EndingTimer <= -450 Then
				DebugLog Lower(SelectedEnding)
				Select Lower(SelectedEnding)
					Case "a1"
						TempSound2 = LoadSound("SFX\EndingA1.ogg")
						PlaySound TempSound2						
					Case "b1"
						TempSound2 = LoadSound("SFX\EndingB1.ogg")
						PlaySound TempSound2
					Case "b2" 
						TempSound2 = LoadSound("SFX\EndingB2.ogg")
						PlaySound TempSound2
					Case "b3"
						TempSound2 = LoadSound("SFX\EndingB3.ogg")
						PlaySound TempSound2
				End Select
			EndIf			
				
		Else
			
			DrawImage EndingScreen, GraphicWidth/2-400, GraphicHeight/2-400
			
			If EndingTimer < -1000 Then 
				
				width = ImageWidth(PauseMenuIMG)
				height = ImageHeight(PauseMenuIMG)
				x = GraphicWidth / 2 - width / 2
				y = GraphicHeight / 2 - height / 2
				
				DrawImage PauseMenuIMG, x, y
				
				Color(255, 255, 255)
				SetFont Font2
				Text(x + width / 2 + 40, y + 20, "THE END", True)
				SetFont Font1
				
				x = x+132
				y = y+122
				
				Local roomamount = 0, roomsfound = 0
				For r.Rooms = Each Rooms
					roomamount = roomamount + 1
					roomsfound = roomsfound + r\found
				Next
				
				Local docamount=0, docsfound=0
				For itt.ItemTemplates = Each ItemTemplates
					If itt\tempname = "paper" Then
						docamount=docamount+1
						docsfound=docsfound+itt\found
					EndIf
				Next
				
				temp=1
				For i = 0 To 14
					temp = temp+Achievements(i)
				Next
				
				Text x, y, "SCPs encountered: " +temp
				Text x, y+20, "Rooms found: " + roomsfound+"/"+roomamount
				Text x, y+40, "Documents discovered: " +docsfound+"/"+docamount
				Text x, y+60, "Items refined in SCP-914: " +RefinedItems
				
				DrawTick (x, y + 100, Achievements(AchvConsole), True)
				Text x+40, y+100, "Didn't use console commands"
				DrawTick (x, y + 130, Contained106, True)
				Text x+40, y+130, "Contained SCP-106"
				DrawTick (x, y + 160, Achievements(AchvPD), True)			
				Text x+40, y+160, "Escaped the pocket dimension"
				DrawTick (x, y + 190, Achievements(AchvTesla), True)				
				Text x+40, y+190, "Lured SCP-106 through a Tesla Gate"
				
				DrawTick (x, y + 220, Achievements(AchvSNAV), True)				
				Text x+40, y+220, "Obtained S-NAV Ultimate"
				DrawTick (x, y + 250, Achievements(AchvOmni), True)				
				Text x+40, y+250, "Obtained Key Card Omni"			
				
				DrawTick (x, y + 280, Achievements(AchvMaynard), True)			
				Text x+40, y+280, "Entered Dr. Maynard's office"
				DrawTick (x, y + 310, Achievements(AchvHarp), True)			
				Text x+40, y+310, "Entered Dr. Harp's office"	
				
				x = GraphicWidth / 2 - width / 2
				y = GraphicHeight / 2 - height / 2
				x = x+width/2
				y = y+height-100
				
				If DrawButton(x-110,y,300,60,"MAIN MENU", True) Then
					TempSound = LoadSound("SFX\breath.ogg")
					PlaySound TempSound
					NullGame()
					MenuOpen = False
					MainMenuOpen = True
					MainMenuTab = 0
					CurrSave = ""
					FlushKeys()
				EndIf
				
			EndIf
			
		EndIf
		
	EndIf
	
	If Fullscreen Then DrawImage CursorIMG, MouseX(),MouseY()
	
	SetFont Font1
End Function

;--------------------------------------- player controls -------------------------------------------
Global MpSendPosition = 0

Function MovePlayer(loaded = True)
	Local Sprint# = 1.0, Speed# = 0.02, i%, angle#
	
	If SuperMan Then
		Speed = Speed * 3
		
		SuperManTimer=SuperManTimer+FPSfactor
		
		CameraShake = Sin(SuperManTimer / 5.0) * (SuperManTimer / 1500.0)
		
		If SuperManTimer > 70 * 50 Then
			Kill()
			ShowEntity Fog
		Else
			BlurTimer = 500		
			HideEntity Fog
		EndIf
	End If
	
	Stamina = Min(Stamina + 0.15 * FPSfactor, 100.0)
	
	For i = 0 To MaxItemAmount-1
		If Inventory(i)<>Null Then
			If Inventory(i)\itemtemplate\tempname = "finevest" Then Stamina = Min(Stamina, 60)
		EndIf
	Next
	
	If Wearing714 Then 
		Stamina = Min(Stamina, 10)
		Sanity = Max(-850, Sanity)
	EndIf
	
	If Abs(CrouchState-Crouch)<0.001 Then 
		CrouchState = Crouch
	Else
		CrouchState = CurveValue(Crouch, CrouchState, 10.0)
	EndIf
	
	If (Not NoClip) Then 
		If (KeyDown(KEY_DOWN) Xor KeyDown(KEY_UP)) Or (KeyDown(KEY_RIGHT) Xor KeyDown(KEY_LEFT)) Or ForceMove>0 Then
			
			If Crouch = 0 And (KeyDown(KEY_SPRINT)) And Stamina > 0.0 Then
				Sprint = 2.5
				Stamina = Stamina - FPSfactor * 0.6
				If Stamina <= 0 Then Stamina = -10.0
			End If
			
			If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then 
				Stamina = 0
				Speed = 0.01
				Sprint = 1.0
			EndIf	
			
			If ForceMove>0 Then Speed=Speed*ForceMove
			
			If SelectedItem<>Null Then
				If SelectedItem\itemtemplate\tempname = "firstaid" Or SelectedItem\itemtemplate\tempname = "finefirstaid" Then 
					Sprint = 0
				ElseIf SelectedItem\itemtemplate\tempname = "firstaid2" 
					Sprint = 0
				EndIf
			EndIf
			
			Local temp# = (Shake Mod 360), tempchn%
			Shake# = (Shake + FPSfactor * Min(Sprint, 1.5) * 7) Mod 720
			If temp < 180 And (Shake Mod 360) >= 180 Then
				If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then
					tempchn% = PlaySound(StepPDSFX(Rand(0, 2)))
					ChannelVolume tempchn, 1.0-(Crouch*0.4)
				Else
					If Sprint = 1.0 Then
						PlayerSoundVolume = Max(4.0,PlayerSoundVolume)
						tempchn% = PlaySound(StepSFX(PlayerRoom\RoomTemplate\StepSound, 1, Rand(0, 3)))
						ChannelVolume tempchn, 1.0-(Crouch*0.6)
					Else
						PlayerSoundVolume = Max(2.5-(Crouch*0.6),PlayerSoundVolume)
						tempchn% = PlaySound(StepSFX(PlayerRoom\RoomTemplate\StepSound, 0, Rand(0, 3)))
						ChannelVolume tempchn, 1.0-(Crouch*0.6)
					End If				
				EndIf
				
			EndIf	
		Else
			;Shake = CurveValue(Shake-(Shake Mod 180),Shake,25.0)
		EndIf
	Else ;noclip on
		If (KeyDown(KEY_SPRINT)) Then 
			Sprint = 2.5
		ElseIf KeyDown(KEY_CROUCH)
			Sprint = 0.5
		EndIf
	EndIf
	
	If KeyHit(KEY_CROUCH) Then Crouch = (Not Crouch)
	
	Local temp2# = (Speed * Sprint * FPSfactor) / (1.0+CrouchState)
	
	If NoClip Then 
		Shake = 0
		CurrSpeed = 0
		CrouchState = 0
		Crouch = 0
		
		RotateEntity Collider, WrapAngle(EntityPitch(Camera)), WrapAngle(EntityYaw(Camera)), 0
		
		temp2 = temp2 * NoClipSpeed
		
		If KeyDown(KEY_DOWN) Then MoveEntity Collider, 0, 0, -temp2
		If KeyDown(KEY_UP) Then MoveEntity Collider, 0, 0, temp2
		
		If KeyDown(KEY_LEFT) Then MoveEntity Collider, -temp2, 0, 0
		If KeyDown(KEY_RIGHT) Then MoveEntity Collider, temp2, 0, 0	
		
		ResetEntity Collider
		If mpState = 1 And loaded And MpSendPosition < 0 Then
			For i% = 1 To MpNumClients - 1
				WriteLine MpClients(i%)\stream,"move"
				WriteLine MpClients(i%)\stream,0
				WriteLine MpClients(i%)\stream,EntityX(Collider)
				WriteLine MpClients(i%)\stream,EntityY(Collider)
				WriteLine MpClients(i%)\stream,EntityZ(Collider)
				WriteLine MpClients(i%)\stream,EntityYaw(Camera)
				WriteLine MpClients(i%)\stream,Crouch
				;DebugLog "Sent movement: "+EntityX(Collider)+", "+EntityY(Collider)+", "+EntityZ(Collider)+"|TIME: "+BlinkTimer
			Next
			MpSendPosition = 8
		EndIf
		
		If mpState = 2 And loaded And MpSendPosition < 0 Then
			WriteLine MpStream,"move"
			WriteLine MpStream,EntityX(Collider)
			WriteLine MpStream,EntityY(Collider)
			WriteLine MpStream,EntityZ(Collider)
			WriteLine MpStream,EntityYaw(Camera)
			WriteLine MpStream,Crouch
			MpSendPosition = 8
			;DebugLog "Sent movement: "+EntityX(Collider)+", "+EntityY(Collider)+", "+EntityZ(Collider)+"|TIME: "+BlinkTimer
		EndIf
	Else
		temp2# = temp2 / Max((Injuries+3.0)/3.0,1.0)
		If Injuries > 0.5 Then 
			temp2 = temp2*Min((Sin(Shake/2)+1.2),1.0)
		EndIf
		
		temp = False
		If KeyDown(KEY_DOWN) Then 
			temp = True 
			angle = 180
			If KeyDown(KEY_LEFT) Then angle = 135 
			If KeyDown(KEY_RIGHT) Then angle = -135 
		ElseIf KeyDown(KEY_UP) Or ForceMove>0
			temp = True
			angle = 0
			If KeyDown(KEY_LEFT) Then angle = 45 
			If KeyDown(KEY_RIGHT) Then angle = -45 
		Else
			If KeyDown(KEY_LEFT) Then angle = 90 : temp = True
			If KeyDown(KEY_RIGHT) Then angle = -90 : temp = True 
		EndIf		
		
		angle = WrapAngle(EntityYaw(Collider,True)+angle+90.0)
		
		If temp Then 
			CurrSpeed = CurveValue(temp2, CurrSpeed, 20.0)
		Else
			CurrSpeed = Max(CurveValue(0.0, CurrSpeed-0.1, 5.0),0.0)
		EndIf
		
		TranslateEntity Collider, Cos(angle)*CurrSpeed, 0, Sin(angle)*CurrSpeed, True
		
		If mpState = 1 And loaded And MpSendPosition < 0 Then
			For i% = 1 To MpNumClients - 1
				WriteLine MpClients(i%)\stream,"move"
				WriteLine MpClients(i%)\stream,0
				WriteLine MpClients(i%)\stream,EntityX(Collider)
				WriteLine MpClients(i%)\stream,EntityY(Collider)
				WriteLine MpClients(i%)\stream,EntityZ(Collider)
				WriteLine MpClients(i%)\stream,EntityYaw(Camera)
				WriteLine MpClients(i%)\stream,Crouch
				;DebugLog "Sent movement: "+EntityX(Collider)+", "+EntityY(Collider)+", "+EntityZ(Collider)+"|TIME: "+BlinkTimer
			Next
			MpSendPosition = 8
		EndIf
		
		If mpState = 2 And loaded And MpSendPosition < 0 Then
			WriteLine MpStream,"move"
			WriteLine MpStream,EntityX(Collider)
			WriteLine MpStream,EntityY(Collider)
			WriteLine MpStream,EntityZ(Collider)
			WriteLine MpStream,EntityYaw(Camera)
			WriteLine MpStream,Crouch
			MpSendPosition = 8
			;DebugLog "Sent movement: "+EntityX(Collider)+", "+EntityY(Collider)+", "+EntityZ(Collider)+"|TIME: "+BlinkTimer
		EndIf
		
		Local CollidedFloor% = False
		For i = 1 To CountCollisions(Collider)
			If CollisionY(Collider, i) < EntityY(Collider) - 0.25 Then CollidedFloor = True
		Next
		
		If CollidedFloor = True Then
			If DropSpeed# < - 0.07 Then 
				If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then
					PlaySound(StepPDSFX(Rand(0, 2)))
				Else
					PlaySound(StepSFX(PlayerRoom\RoomTemplate\StepSound, 0, Rand(0, 3)))
				EndIf
				PlayerSoundVolume = Max(3.0,PlayerSoundVolume)
			EndIf
			DropSpeed# = 0
		Else
			DropSpeed# = Min(Max(DropSpeed - 0.006 * FPSfactor, -2.0), 0.0)
		EndIf	
		
		TranslateEntity Collider, 0, DropSpeed, 0
	EndIf
	
	ForceMove = False
	
	If Injuries > 1.0 Then
		temp2 = Bloodloss
		BlurTimer = Max(Max(Sin(MilliSecs()/100.0)*Bloodloss*30.0,Bloodloss*2*(2.0-CrouchState)),BlurTimer)
		Bloodloss = Min(Bloodloss + (Min(Injuries,3.5)/300.0)*FPSfactor,100)
		
		If temp2 <= 60 And Bloodloss > 60 Then
			Msg = "You are feeling weak from the blood loss"
			MsgTimer = 70*4
		EndIf
	EndIf
	
	UpdateInfect()
	
	If Bloodloss > 0 Then
		If Rnd(200)<Min(Injuries,4.0) Then
			pvt = CreatePivot()
			PositionEntity pvt, EntityX(Collider)+Rnd(-0.05,0.05),EntityY(Collider)-0.05,EntityZ(Collider)+Rnd(-0.05,0.05)
			TurnEntity pvt, 90, 0, 0
			EntityPick(pvt,0.3)
			de.decals = CreateDecal(Rand(15,16), PickedX(), PickedY()+0.005, PickedZ(), 90, Rand(360), 0)
			de\size = Rnd(0.03,0.08)*Min(Injuries,3.0) : EntityAlpha(de\obj, 1.0) : ScaleSprite de\obj, de\size, de\size
			tempchn% = PlaySound (DripSFX(Rand(0,2)))
			ChannelVolume tempchn, Rnd(0.0,0.8)
			ChannelPitch tempchn, Rand(20000,30000)
			;EndIf
			FreeEntity pvt
		EndIf
		
		CurrCameraZoom = Max(CurrCameraZoom, (Sin(Float(MilliSecs())/20.0)+1.0)*Bloodloss*0.2)
		
		If Bloodloss > 60 Then Crouch = True
		If Bloodloss => 100 Then 
			Kill()
			HeartBeatVolume = 0.0
		ElseIf Bloodloss > 80.0
			HeartBeatRate = Max(150-(Bloodloss-80)*5,HeartBeatRate)
			HeartBeatVolume = Max(HeartBeatVolume, 0.75+(Bloodloss-80.0)*0.0125)	
		ElseIf Bloodloss > 35.0
			HeartBeatRate = Max(70+Bloodloss,HeartBeatRate)
			HeartBeatVolume = Max(HeartBeatVolume, (Bloodloss-35.0)/60.0)			
		EndIf
	EndIf
		
	If KeyHit(KEY_BLINK) Then BlinkTimer = 0
	If KeyDown(KEY_BLINK) And BlinkTimer < - 10 Then BlinkTimer = -10
	
	If HeartBeatVolume > 0 Then
		If HeartBeatTimer <= 0 Then
			tempchn = PlaySound (HeartBeatSFX)
			ChannelVolume tempchn, HeartBeatVolume
			
			HeartBeatTimer = 70.0*(60.0/Max(HeartBeatRate,1.0))
		Else
			HeartBeatTimer = HeartBeatTimer - FPSfactor
		EndIf
		
		HeartBeatVolume = Max(HeartBeatVolume - FPSfactor*0.1, 0)
	EndIf
	
	If mpState <> 0 Then
		MpSendPosition = MpSendPosition - 1
	EndIf
	
	
End Function

Function MouseLook()
	Local i%
	
	CameraShake = Max(CameraShake - (FPSfactor / 10), 0)
	
	;CameraZoomTemp = CurveValue(CurrCameraZoom,CameraZoomTemp, 5.0)
	CameraZoom(Camera, Min(1.0+(CurrCameraZoom/400.0),1.1))
	CurrCameraZoom = Max(CurrCameraZoom - FPSfactor, 0)
	
	If KillTimer >= 0 And FallTimer >=0 Then
		
		HeadDropSpeed = 0
		
		;If 0 Then 
		;fixing the black screen bug with some bubblegum code 
		Local Zero# = 0.0
		Local Nan1# = 0.0 / Zero
		If Int(EntityX(Collider))=Int(Nan1) Then
			
			PositionEntity Collider, EntityX(Camera, True), EntityY(Camera, True) - 0.5, EntityZ(Camera, True), True
			Msg = "EntityX(Collider) = NaN, RESETTING COORDINATES    -    New coordinates: "+EntityX(Collider)
			MsgTimer = 300				
		EndIf
		;EndIf
		
		Local up# = (Sin(Shake) / (20.0+CrouchState*20.0))*0.6;, side# = Cos(Shake / 2.0) / 35.0		
		Local roll# = Max(Min(Sin(Shake/2)*2.5*Min(Injuries+0.25,3.0),8.0),-8.0)
		
		;k‰‰nnet‰‰n kameraa sivulle jos pelaaja on vammautunut
		;RotateEntity Collider, EntityPitch(Collider), EntityYaw(Collider), Max(Min(up*30*Injuries,50),-50)
		PositionEntity Camera, EntityX(Collider), EntityY(Collider), EntityZ(Collider)
		RotateEntity Camera, 0, EntityYaw(Collider), roll*0.5
		
		MoveEntity Camera, side, up + 0.6 + CrouchState * -0.3, 0
		
		;RotateEntity Collider, EntityPitch(Collider), EntityYaw(Collider), 0
		;moveentity player, side, up, 0	
		; -- Update the smoothing que To smooth the movement of the mouse.
		mouse_x_speed_1# = CurveValue(MouseXSpeed() * (MouseSens + 0.6) , mouse_x_speed_1, 6.0 / (MouseSens + 1.0)) 
		If Int(mouse_x_speed_1) = Int(Nan1) Then mouse_x_speed_1 = 0
		
		If InvertMouse Then
			mouse_y_speed_1# = CurveValue(-MouseYSpeed() * (MouseSens + 0.6), mouse_y_speed_1, 6.0/(MouseSens+1.0)) 
		Else
			mouse_y_speed_1# = CurveValue(MouseYSpeed () * (MouseSens + 0.6), mouse_y_speed_1, 6.0/(MouseSens+1.0)) 
		EndIf
		If Int(mouse_y_speed_1) = Int(Nan1) Then mouse_y_speed_1 = 0		
		
		Local the_yaw# = ((mouse_x_speed_1#)) * mouselook_x_inc# / (1.0+WearingVest)
		Local the_pitch# = ((mouse_y_speed_1#)) * mouselook_y_inc# / (1.0+WearingVest)
		
		TurnEntity Collider, 0.0, -the_yaw#, 0.0 ; Turn the user on the Y (yaw) axis.
		user_camera_pitch# = user_camera_pitch# + the_pitch#
		; -- Limit the user;s camera To within 180 degrees of pitch rotation. ;EntityPitch(); returns useless values so we need To use a variable To keep track of the camera pitch.
		If user_camera_pitch# > 70.0 Then user_camera_pitch# = 70.0
		If user_camera_pitch# < - 70.0 Then user_camera_pitch# = -70.0
		
		RotateEntity Camera, WrapAngle(user_camera_pitch + Rnd(-CameraShake, CameraShake)), WrapAngle(EntityYaw(Collider) + Rnd(-CameraShake, CameraShake)), roll ; Pitch the user;s camera up And down.
		
		If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then
			RotateEntity Camera, WrapAngle(EntityPitch(Camera)),WrapAngle(EntityYaw(Camera)), roll+WrapAngle(Sin(MilliSecs()/150.0)*30.0) ; Pitch the user;s camera up And down.
		EndIf
		
	Else
		HideEntity Collider
		PositionEntity Camera, EntityX(Head), EntityY(Head), EntityZ(Head)
		;rotateentity camera, entityPitch(head), entityyaw(head), entityRoll(head)
		
		Local CollidedFloor% = False
		For i = 1 To CountCollisions(Head)
			If CollisionY(Head, i) < EntityY(Head) - 0.01 Then CollidedFloor = True
		Next
		
		If InvertMouse Then
			TurnEntity (Camera, -MouseYSpeed() * 0.05 * FPSfactor, -MouseXSpeed() * 0.15 * FPSfactor, 0)
		Else
			TurnEntity (Camera, MouseYSpeed() * 0.05 * FPSfactor, -MouseXSpeed() * 0.15 * FPSfactor, 0)
		End If
		
		If CollidedFloor = True Then
			;If DropSpeed# < - 0.09 Then KillTimer = Max(1, KillTimer)
			HeadDropSpeed# = 0
			DebugLog "collfloor"
		Else
			
			If KillAnim = 0 Then 
				MoveEntity Head, 0, 0, HeadDropSpeed
				RotateEntity(Head, CurveAngle(-90.0, EntityPitch(Head), 20.0), EntityYaw(Head), EntityRoll(Head))
				RotateEntity(Camera, CurveAngle(EntityPitch(Head) - 40.0, EntityPitch(Camera), 40.0), EntityYaw(Camera), EntityRoll(Camera))
			Else
				MoveEntity Head, 0, 0, -HeadDropSpeed
				RotateEntity(Head, CurveAngle(90.0, EntityPitch(Head), 20.0), EntityYaw(Head), EntityRoll(Head))
				RotateEntity(Camera, CurveAngle(EntityPitch(Head) + 40.0, EntityPitch(Camera), 40.0), EntityYaw(Camera), EntityRoll(Camera))
			EndIf
			
			;rotateentity(camera, CurveAngle(entityPitch(head) - 180, entityPitch(camera), 40.0), CurveAngle(entityyaw(head) - 180, entityyaw(camera), 40.0), CurveAngle(entityRoll(head) - 180, entityRoll(camera), 40.0))
			
			HeadDropSpeed# = HeadDropSpeed - 0.002 * FPSfactor
		EndIf
		
	EndIf
	
	;pˆlyhiukkasia
	If Rand(35) = 1 Then
		Local pvt% = CreatePivot()
		PositionEntity(pvt, EntityX(Camera, True), EntityY(Camera, True), EntityZ(Camera, True))
		RotateEntity(pvt, 0, Rnd(360), 0)
		If Rand(2) = 1 Then
			MoveEntity(pvt, 0, Rnd(-0.5, 0.5), Rnd(0.5, 1.0))
		Else
			MoveEntity(pvt, 0, Rnd(-0.5, 0.5), Rnd(0.5, 1.0))
		End If
		
		Local p.Particles = CreateParticle(EntityX(pvt), EntityY(pvt), EntityZ(pvt), 2, 0.002, 0, 300)
		p\speed = 0.001
		RotateEntity(p\pvt, Rnd(-20, 20), Rnd(360), 0)
		
		p\SizeChange = -0.00001
		
		FreeEntity pvt
	End If
	
	; -- Limit the mouse;s movement. Using this method produces smoother mouselook movement than centering the mouse Each loop.
	If (MouseX() > mouse_right_limit) Or (MouseX() < mouse_left_limit) Or (MouseY() > mouse_bottom_limit) Or (MouseY() < mouse_top_limit)
		MoveMouse viewport_center_x, viewport_center_y
	EndIf
	
	If WearingGasMask Or WearingHazmat Then
		If WearingGasMask = 2 Then Stamina = Min(100, Stamina + (100.0-Stamina)*0.01*FPSfactor)
		If WearingHazmat = 2 Then 
			Stamina = Min(100, Stamina + (100.0-Stamina)*0.01*FPSfactor)
		ElseIf WearingHazmat=1
			Stamina = Min(60, Stamina)
		EndIf
		
		ShowEntity(GasMaskOverlay)
		If GasmaskBreathCHN = 0 Then
			GasmaskBreathCHN = PlaySound(GasmaskBreath)
		Else
			If Not ChannelPlaying(GasmaskBreathCHN) Then GasmaskBreathCHN = PlaySound(GasmaskBreath)
		End If
	Else
		If GasmaskBreathCHN <> 0 Then
			If ChannelPlaying(GasmaskBreathCHN) Then StopChannel(GasmaskBreathCHN)
		End If	
		HideEntity(GasMaskOverlay)
	End If
	
	For i = 0 To 5
		If SCP1025state[i]>0 Then
			Select i
				Case 0 ;common cold
					If FPSfactor>0 Then 
						If Rand(1000)=1 Then
							If CoughCHN = 0 Then
								CoughCHN = PlaySound(CoughSFX(Rand(0, 2)))
							Else
								If Not ChannelPlaying(CoughCHN) Then CoughCHN = PlaySound(CoughSFX(Rand(0, 2)))
							End If
						EndIf
					EndIf
					Stamina = Stamina - FPSfactor * 0.3
				Case 1 ;chicken pox
					If Rand(9000)=1 And Msg="" Then
						Msg="Your skin is feeling itchy"
						MsgTimer =70*4
					EndIf
				Case 2 ;cancer of the lungs
					If FPSfactor>0 Then 
						If Rand(800)=1 Then
							If CoughCHN = 0 Then
								CoughCHN = PlaySound(CoughSFX(Rand(0, 2)))
							Else
								If Not ChannelPlaying(CoughCHN) Then CoughCHN = PlaySound(CoughSFX(Rand(0, 2)))
							End If
						EndIf
					EndIf
					Stamina = Stamina - FPSfactor * 0.1
				Case 3 ;appendicitis
					;0.035/sec = 2.1/min
					SCP1025state[i]=SCP1025state[i]+FPSfactor*0.0005
					If SCP1025state[i]>20.0 Then
						If SCP1025state[i]-FPSfactor<=20.0 Then Msg="The pain in your stomach is getting unbearable"
						Stamina = Stamina - FPSfactor * 0.3
					ElseIf SCP1025state[i]>10.0
						If SCP1025state[i]-FPSfactor<=10.0 Then Msg="Your stomach is aching"
					EndIf
				Case 4 ;asthma
					If Stamina < 35 Then
						If Rand(Int(140+Stamina*8))=1 Then
							If CoughCHN = 0 Then
								CoughCHN = PlaySound(CoughSFX(Rand(0, 2)))
							Else
								If Not ChannelPlaying(CoughCHN) Then CoughCHN = PlaySound(CoughSFX(Rand(0, 2)))
							End If
						EndIf
						CurrSpeed = CurveValue(0, CurrSpeed, 10+Stamina*15)
					EndIf
				Case 5;cardiac arrest
					SCP1025state[i]=SCP1025state[i]+FPSfactor*0.35
					;35/sec
					If SCP1025state[i]>110 Then
						HeartBeatRate=0
						BlurTimer = Max(BlurTimer, 500)
						If SCP1025state[i]>140 Then Kill()
					Else
						HeartBeatRate=Max(HeartBeatRate, 70+SCP1025state[i])
						HeartBeatVolume = 1.0
					EndIf
			End Select 
		EndIf
	Next
	
	
End Function

;--------------------------------------- GUI, menu etc ------------------------------------------------

Function DrawGUI()
	
	If MenuOpen Or SelectedDoor <> Null Or InvOpen Or EndingTimer < 0 Then
		ShowPointer()
	Else
		HidePointer()
	EndIf 
	
	Local temp%, x%, y%, z%, i%, yawvalue#, pitchvalue#
	Local n%, xtemp, ytemp
	
	If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then
		For e.events = Each Events
			If e\room = PlayerRoom Then
				If e\EventState > 600 Then
					If BlinkTimer < -3 And BlinkTimer > -11 Then
						If e\img = 0 Then
							If BlinkTimer > -5 And Rand(30)=1 Then
								If Rand(5)<5 Then 
									e\sound = LoadSound("SFX\Horror12.ogg")	
									PlaySound e\sound
								EndIf
								e\img = LoadImage("GFX\npcs\106face.jpg")
							EndIf
						Else
							DrawImage e\img, GraphicWidth/2-Rand(390,310), GraphicHeight/2-Rand(290,310)
						EndIf
					Else
						If e\img <> 0 Then FreeImage e\img : e\img = 0
					EndIf						
				EndIf						
			EndIf
		Next
		
	EndIf
	
	
	If ClosestButton <> 0 And SelectedDoor = Null And InvOpen = False Then
		temp% = CreatePivot()
		PositionEntity temp, EntityX(Camera), EntityY(Camera), EntityZ(Camera)
		PointEntity temp, ClosestButton
		yawvalue# = WrapAngle(EntityYaw(Camera) - EntityYaw(temp))
		If yawvalue > 90 And yawvalue <= 180 Then yawvalue = 90
		If yawvalue > 180 And yawvalue < 270 Then yawvalue = 270
		pitchvalue# = WrapAngle(EntityPitch(Camera) - EntityPitch(temp))
		If pitchvalue > 90 And pitchvalue <= 180 Then pitchvalue = 90
		If pitchvalue > 180 And pitchvalue < 270 Then pitchvalue = 270
		
		FreeEntity (temp)
		
		DrawImage(HandIcon, GraphicWidth / 2 + Sin(yawvalue) * (GraphicWidth / 3) - 32, GraphicHeight / 2 - Sin(pitchvalue) * (GraphicHeight / 3) - 32)
		
		If MouseUp1 Then
			MouseUp1 = False
			If ClosestDoor <> Null Then 
				If ClosestDoor\Code <> "" Then
					SelectedDoor = ClosestDoor
				Else
					PlaySound2(ButtonSFX, Camera, ClosestButton)
					UseDoor(ClosestDoor,True)				
				EndIf
			EndIf
		EndIf
	EndIf
	
	If SelectedScreen <> Null Then
		DrawImage SelectedScreen\img, GraphicWidth/2-ImageWidth(SelectedScreen\img)/2,GraphicHeight/2-ImageHeight(SelectedScreen\img)/2
		
		If MouseUp1 Or MouseHit2 Then
			FreeImage SelectedScreen\img : SelectedScreen\img = 0
			SelectedScreen = Null
			MouseUp1 = False
		EndIf
	EndIf
	
	If ClosestItem <> Null Then
		temp = CreatePivot()
		PositionEntity temp, EntityX(Camera), EntityY(Camera), EntityZ(Camera)
		PointEntity temp, ClosestItem\obj
		yawvalue# = WrapAngle(EntityYaw(Camera) - EntityYaw(temp))
		If yawvalue > 90 And yawvalue <= 180 Then yawvalue = 90
		If yawvalue > 180 And yawvalue < 270 Then yawvalue = 270
		pitchvalue# = WrapAngle(EntityPitch(Camera) - EntityPitch(temp))
		If pitchvalue > 90 And pitchvalue <= 180 Then pitchvalue = 90
		If pitchvalue > 180 And pitchvalue < 270 Then pitchvalue = 270
		
		FreeEntity (temp)
		
		DrawImage(HandIcon, GraphicWidth / 2 + Sin(yawvalue) * (GraphicWidth / 3) - 32, GraphicHeight / 2 - Sin(pitchvalue) * (GraphicHeight / 3) - 32)
	EndIf
	
	If DrawHandIcon Then DrawImage(HandIcon, GraphicWidth / 2 - 32, GraphicHeight / 2 - 32)
	For i = 0 To 3
		If DrawArrowIcon(i) Then
			x = GraphicWidth / 2 - 32
			y = GraphicHeight / 2 - 32		
			Select i
				Case 0
					y = y - 64 - 5
				Case 1
					x = x + 64 + 5
				Case 2
					y = y + 64 + 5
				Case 3
					x = x - 5 - 64
			End Select
			DrawImage(HandIcon, x, y)
			Color 0, 0, 0
			Rect(x + 4, y + 4, 64 - 8, 64 - 8)
			DrawImage(ArrowIMG(i), x + 21, y + 21)
			DrawArrowIcon(i) = False
		End If
	Next
	
	If HUDenabled Then 
		
		Local width% = 204, height% = 20
		x% = 80
		y% = GraphicHeight - 95
		
		Color 255, 255, 255	
		Rect (x, y, width, height, False)
		Color 87, 101, 105
		If BlinkTimer < 60 Then Color 200, 0, 0
		For i = 1 To Int(((width - 2) * (BlinkTimer / (BLINKFREQ))) / 10)
			DrawImage(BlinkMeterIMG, x + 3 + 10 * (i - 1), y + 3)
		Next	
		Color 0, 0, 0
		Rect(x - 50, y, 30, 30)
		
		If EyeIrritation > 0 Then
			Color 200, 0, 0
			Rect(x - 50 - 3, y - 3, 30 + 6, 30 + 6)
		End If
		
		Color 255, 255, 255
		Rect(x - 50 - 1, y - 1, 30 + 2, 30 + 2, False)
		
		DrawImage BlinkIcon, x - 50, y
		
		y = GraphicHeight - 55
		Color 255, 255, 255
		Rect (x, y, width, height, False)
		Color 53, 68, 37
		If Stamina < 10 Then Color 200, 0, 0
		For i = 1 To Int(((width - 2) * (Stamina / 100.0)) / 10)
			DrawImage(StaminaMeterIMG, x + 3 + 10 * (i - 1), y + 3)
		Next	
		
		Color 0, 0, 0
		Rect(x - 50, y, 30, 30)
		
		Color 255, 255, 255
		Rect(x - 50 - 1, y - 1, 30 + 2, 30 + 2, False)
		If Crouch Then
			DrawImage CrouchIcon, x - 50, y
		Else
			DrawImage SprintIcon, x - 50, y
		EndIf
		
		If DebugHUD Then
			Color 255, 255, 255
			
			;Text x + 250, 50, "Zone: " + (EntityZ(Collider)/8.0)
			Text x - 50, 50, "Player Position: (" + f2s(EntityX(Collider), 3) + ", " + f2s(EntityY(Collider), 3) + ", " + f2s(EntityZ(Collider), 3) + ")"
			Text x - 50, 70, "Camera Position: (" + f2s(EntityX(Camera), 3)+ ", " + f2s(EntityY(Camera), 3) +", " + f2s(EntityZ(Camera), 3) + ")"
			Text x - 50, 100, "Player Rotation: (" + f2s(EntityPitch(Collider), 3) + ", " + f2s(EntityYaw(Collider), 3) + ", " + f2s(EntityRoll(Collider), 3) + ")"
			Text x - 50, 120, "Camera Rotation: (" + f2s(EntityPitch(Camera), 3)+ ", " + f2s(EntityYaw(Camera), 3) +", " + f2s(EntityRoll(Camera), 3) + ")"
			Text x - 50, 150, "Room: " + PlayerRoom\RoomTemplate\Name
			For ev.Events = Each Events
				If ev\room = PlayerRoom Then
					Text x - 50, 170, "Room event: " + ev\EventName   
					Text x - 50, 190, "state: " + ev\EventState
					Text x - 50, 210, "state2: " + ev\EventState2   
					Text x - 50, 230, "state3: " + ev\EventState3
					Exit
				EndIf
			Next
			Text x - 50, 250, "Room coordinates: (" + Floor(EntityX(PlayerRoom\obj) / 8.0 + 0.5) + ", " + Floor(EntityZ(PlayerRoom\obj) / 8.0 + 0.5) + ")"
			Text x - 50, 280, "Stamina: " + f2s(Stamina, 3)
			Text x - 50, 300, "Death timer: " + f2s(KillTimer, 3)               
			Text x - 50, 320, "Blink timer: " + f2s(BlinkTimer, 3)
			Text x - 50, 340, "Injuries: " + Injuries
			Text x - 50, 360, "Bloodloss: " + Bloodloss
			Text x - 50, 390, "SCP - 173 Position: (" + f2s(EntityX(Curr173\obj), 3) + ", " + f2s(EntityY(Curr173\obj), 3) + ", " + f2s(EntityZ(Curr173\obj), 3) + ")"
			Text x - 50, 410, "SCP - 173 Idle: " + Curr173\Idle
			Text x - 50, 430, "SCP - 173 State: " + Curr173\State
			Text x - 50, 450, "SCP - 106 Position: (" + f2s(EntityX(Curr106\obj), 3) + ", " + f2s(EntityY(Curr106\obj), 3) + ", " + f2s(EntityZ(Curr106\obj), 3) + ")"
			Text x - 50, 470, "SCP - 106 Idle: " + Curr106\Idle
			Text x - 50, 490, "SCP - 106 State: " + Curr106\State
			Local offset% = 0
			For npc.NPCs = Each NPCs
				If npc\NPCtype = NPCtype096 Then
					Text x - 50, 510, "SCP - 096 Position: (" + f2s(EntityX(npc\obj), 3) + ", " + f2s(EntityY(npc\obj), 3) + ", " + f2s(EntityZ(npc\obj), 3) + ")"
					Text x - 50, 530, "SCP - 096 Idle: " + npc\Idle
					Text x - 50, 550, "SCP - 096 State: " + npc\State
					Text x - 50, 570, "SCP - 096 Speed: " + f2s(npc\currspeed, 5)
				EndIf
				If npc\NPCtype = NPCtypeMTF Then
					Text x - 50, 600 + 60 * offset, "MTF " + offset + " Position: (" + f2s(EntityX(npc\obj), 3) + ", " + f2s(EntityY(npc\obj), 3) + ", " + f2s(EntityZ(npc\obj), 3) + ")"
					Text x - 50, 640 + 60 * offset, "MTF " + offset + " State: " + npc\State
					Text x - 50, 620 + 60 * offset, "MTF " + offset + " LastSeen: " + npc\lastseen					
					offset = offset + 1
				EndIf
			Next
			
		EndIf
		
	EndIf
		
	Local PrevInvOpen% = InvOpen, MouseSlot% = 66
	
	If SelectedDoor <> Null Then
		SelectedItem = Null
		
		x = GraphicWidth/2-ImageWidth(KeypadHUD)/2
		y = GraphicHeight/2-ImageHeight(KeypadHUD)/2
		
		DrawImage KeypadHUD, x, y
		
		If KeypadMSG <> "" Then 
			KeypadTimer = KeypadTimer-FPSfactor2
			
			If (KeypadTimer Mod 70) < 35 Then Text GraphicWidth/2, y+124, KeypadMSG, True,True
			If KeypadTimer =<0 Then
				KeypadMSG = ""
				SelectedDoor = Null
			EndIf
		Else
			SetFont Font2
			Text GraphicWidth/2, y+124, KeypadInput,True,True	
			SetFont Font1
			Text GraphicWidth/2, y+70, "Access code: ",True,True		
		EndIf
		
		x = x+42
		y = y+233
		
		For n = 0 To 3
			For i = 0 To 2
				xtemp = x+61*n
				ytemp = y+53*i
				
				temp = False
				If MouseOn(xtemp,ytemp, 54,51) And KeypadMSG = "" Then
					If MouseUp1 Then 
						Select (n+1)+(i*4)
							Case 1,2,3
								KeypadInput=KeypadInput + ((n+1)+(i*4))
							Case 4
								KeypadInput=KeypadInput + "0"
							Case 5,6,7
								KeypadInput=KeypadInput + ((n+1)+(i*4)-1)
							Case 8 ;enter
								DebugLog SelectedDoor\Code
								DebugLog KeypadInput
								If KeypadInput = SelectedDoor\Code Then
									If SelectedDoor\Code = Str(AccessCode) Then
										Achievements(AchvMaynard) = True
									ElseIf SelectedDoor\Code = "7816"
										Achievements(AchvHarp) = True	
									EndIf									
									
									SelectedDoor\locked = 0
									UseDoor(SelectedDoor,True)
									SelectedDoor = Null
								Else
									KeypadMSG = "ACCESS DENIED"
									KeypadTimer = 210
									KeypadInput = ""	
								EndIf
							Case 9,10,11
								KeypadInput=KeypadInput + ((n+1)+(i*4)-2)
							Case 12
								KeypadInput = ""
						End Select 
						If Len(KeypadInput)> 4 Then KeypadInput = Left(KeypadInput,4)
					EndIf
					
					If MouseDown1 Then temp = True
				Else
					temp = False
				EndIf
				
				If temp = True Then
					DrawImage ButtonDown, xtemp,ytemp
				Else
					DrawImage ButtonUp, xtemp,ytemp	
				EndIf
			Next
		Next
		
		If Fullscreen Then DrawImage CursorIMG, MouseX(),MouseY()
		
		If MouseHit2 Then 
			SelectedDoor = Null
		EndIf
	Else
		KeypadInput = ""
		KeypadTimer = 0
		KeypadMSG= ""
	EndIf
	
	If KeyHit(1) And EndingTimer = 0 Then 
		If MenuOpen Or InvOpen Then
			ResumeSounds()
		Else
			PauseSounds()
		EndIf
		MenuOpen = (Not MenuOpen)
		SelectedDoor = Null
		SelectedScreen = Null
	EndIf
	
	If InvOpen Then
		SelectedDoor = Null
		
		width% = 70
		height% = 70
		Local spacing% = 35
		
		x = GraphicWidth / 2 - (width * MaxItemAmount /2 + spacing * (MaxItemAmount / 2 - 1)) / 2
		y = GraphicHeight / 2 - height
		
		ItemAmount = 0
		For  n% = 0 To MaxItemAmount - 1
			Local MouseOn% = False
			If MouseX() > x And MouseX() < x + width Then
				If MouseY() > y And MouseY() < y + height Then
					MouseOn = True
				End If
			EndIf
			
			If Inventory(n) <> Null Then
				Color 200, 200, 200
				Select Inventory(n)\itemtemplate\tempname 
					Case "gasmask"
						If WearingGasMask=1 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "supergasmask"
						If WearingGasMask=2 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "hazmatsuit"
						If WearingHazmat=1 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "hazmatsuit2"
						If WearingHazmat=2 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "vest"
						If WearingVest=1 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "finevest"
						If WearingVest=2 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "scp714"
						If Wearing714 Then Rect(x - 3, y - 3, width + 6, height + 6)
				End Select
			EndIf
			
			If MouseOn Then
				MouseSlot = n
				Color 255, 0, 0
				Rect(x - 1, y - 1, width + 2, height + 2)
			EndIf
			
			Color 255, 255, 255
			Rect(x, y, width, height, False)
			Color 125, 125, 125
			Rect(x + 1, y + 1, width - 2, height - 2, False)
			Color 0, 0, 0
			Rect(x + 5, y + 5, width - 10, height - 10)
			
			If Inventory(n) <> Null Then
				If (SelectedItem <> Inventory(n) Or MouseOn) Then DrawImage(Inventory(n)\itemtemplate\invimg, x + width / 2 - 32, y + height / 2 - 32)
			EndIf
			
			If Inventory(n) <> Null And SelectedItem <> Inventory(n) Then
				;drawimage(Inventory(n).InvIMG, x + width / 2 - 32, y + height / 2 - 32)
				If MouseOn Then
					Color 255, 255, 255	
					Text(x + width / 2, y + height + spacing - 15, Inventory(n)\itemtemplate\name, True)				
					If SelectedItem = Null Then
						If MouseHit1 Then
							SelectedItem = Inventory(n)
							MouseHit1 = False
							
							If DoubleClick Then
								If Inventory(n)\itemtemplate\sound <> 66 Then PlaySound(PickSFX(Inventory(n)\itemtemplate\sound))
								InvOpen = False
								DoubleClick = False
							EndIf
							
						EndIf
					Else
						
					EndIf
				EndIf
				
				ItemAmount=ItemAmount+1
			Else
				If MouseOn And MouseHit1 Then
					For z% = 0 To MaxItemAmount - 1
						If Inventory(z) = SelectedItem Then Inventory(z) = Null
					Next
					Inventory(n) = SelectedItem
				End If
				
			EndIf					
			
			x=x+width + spacing
			If n = 4 Then 
				y = y + height*2 
				x = GraphicWidth / 2 - (width * MaxItemAmount /2 + spacing * (MaxItemAmount / 2 - 1)) / 2
			EndIf
		Next
		
		If SelectedItem <> Null Then
			If MouseDown1 Then
				If MouseSlot = 66 Then
					DrawImage(SelectedItem\itemtemplate\invimg, MouseX() - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, MouseY() - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
				ElseIf SelectedItem <> Inventory(MouseSlot)
					DrawImage(SelectedItem\itemtemplate\invimg, MouseX() - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, MouseY() - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
				EndIf
			Else
				If MouseSlot = 66 Then
					If SelectedItem\itemtemplate\sound <> 66 Then PlaySound(PickSFX(SelectedItem\itemtemplate\sound))
					
					ShowEntity(SelectedItem\obj)
					PositionEntity(SelectedItem\obj, EntityX(Camera), EntityY(Camera), EntityZ(Camera))
					RotateEntity(SelectedItem\obj, EntityPitch(Camera), EntityYaw(Camera), 0)
					MoveEntity(SelectedItem\obj, 0, -0.1, 0.1)
					RotateEntity(SelectedItem\obj, 0, Rand(360), 0)
					ResetEntity (SelectedItem\obj)
					SelectedItem\Picked = False
					
					offset% = 0
					For it.Items = Each Items
						If it = SelectedItem
							Exit
						EndIf
						offset = offset + 1
					Next
					
					If mpState = 1
						For iter% = 1 To MpNumClients - 1
							WriteLine MpClients(iter%)\stream, "itemshow"
							WriteLine MpClients(iter%)\stream, offset
							WriteLine MpClients(iter%)\stream, EntityX(Camera, True)
							WriteLine MpClients(iter%)\stream, EntityY(Camera, True) - 0.1
							WriteLine MpClients(iter%)\stream, EntityZ(Camera, True) + 0.1
						Next
					EndIf
					
					If mpState = 2 Then
						WriteLine MpStream,"itemshow"
						WriteLine MpStream,offset
						WriteLine MpStream, EntityX(Camera, True)
						WriteLine MpStream, EntityY(Camera, True) - 0.1
						WriteLine MpStream, EntityZ(Camera, True) + 0.1
						
					EndIf
					
					For z% = 0 To MaxItemAmount - 1
						If Inventory(z) = SelectedItem Then Inventory(z) = Null
					Next
					If SelectedItem\itemtemplate\tempname = "gasmask" Or SelectedItem\itemtemplate\tempname = "supergasmask" Then 
						Msg = "You take off the Gas Mask"
						MsgTimer = 70*5
						WearingGasMask = False
					EndIf
					If SelectedItem\itemtemplate\tempname = "hazmatsuit" Or SelectedItem\itemtemplate\tempname = "hazmatsuit2" Then 
						Msg = "You take off the hazmat suit"
						MsgTimer = 70*5
						WearingHazmat = False
					EndIf
					If SelectedItem\itemtemplate\tempname = "vest" Or SelectedItem\itemtemplate\tempname = "finevest" Then 
						Msg = "You take off the "+SelectedItem\itemtemplate\name
						MsgTimer = 70*5
						WearingVest = False
					EndIf
					
					SelectedItem = Null
					InvOpen = False
					
					MoveMouse viewport_center_x, viewport_center_y
				Else
					
					If Inventory(MouseSlot) = Null Then
						For z% = 0 To MaxItemAmount - 1
							If Inventory(z) = SelectedItem Then Inventory(z) = Null
						Next
						Inventory(MouseSlot) = SelectedItem
						SelectedItem = Null
					ElseIf Inventory(MouseSlot) <> SelectedItem
						Select SelectedItem\itemtemplate\tempname
							Case "battery", "bat"
								Select Inventory(MouseSlot)\itemtemplate\name
									Case "S-NAV Navigator", "S-NAV 300 Navigator", "S-NAV 310 Navigator"
										If SelectedItem\itemtemplate\sound <> 66 Then PlaySound(PickSFX(SelectedItem\itemtemplate\sound))	
										RemoveItem (SelectedItem)
										SelectedItem = Null
										Inventory(MouseSlot)\state = 100.0
										Msg = "You replaced the battery of the navigator"
										MsgTimer = 70 * 5
									Case "S-NAV Navigator Ultimate"
										Msg = "There seems to be no place for batteries in the navigator"
										MsgTimer = 70 * 5
									Case "Radio Transceiver"
										Select Inventory(MouseSlot)\itemtemplate\tempname 
											Case "fineradio", "veryfineradio"
												Msg = "There seems to be no place for batteries in the radio"
												MsgTimer = 70 * 5
											Case "18vradio"
												Msg = "The battery doesn't seem to fit"
												MsgTimer = 70 * 5
											Case "radio"
												If SelectedItem\itemtemplate\sound <> 66 Then PlaySound(PickSFX(SelectedItem\itemtemplate\sound))	
												RemoveItem (SelectedItem)
												SelectedItem = Null
												Inventory(MouseSlot)\state = 100.0
												Msg = "You replaced the battery of the radio"
												MsgTimer = 70 * 5
										End Select
									Default
										DebugLog Inventory(MouseSlot)\itemtemplate\name
										Msg = "This item can't be used this way"
										MsgTimer = 70 * 5	
								End Select
							Case "18vbat"
								Select Inventory(MouseSlot)\itemtemplate\name
									Case "S-NAV Navigator", "S-NAV 300 Navigator", "S-NAV 310 Navigator"
										Msg = "The battery doesn't seem to fit"
										MsgTimer = 70 * 5
									Case "S-NAV Navigator Ultimate"
										Msg = "There seems to be no place for batteries in the navigator"
										MsgTimer = 70 * 5
									Case "Radio Transceiver"
										Select Inventory(MouseSlot)\itemtemplate\tempname 
											Case "fineradio", "veryfineradio"
												Msg = "There seems to be no place for batteries in the radio"
												MsgTimer = 70 * 5		
											Case "18vradio"
												If SelectedItem\itemtemplate\sound <> 66 Then PlaySound(PickSFX(SelectedItem\itemtemplate\sound))	
												RemoveItem (SelectedItem)
												SelectedItem = Null
												Inventory(MouseSlot)\state = 100.0
												Msg = "You replaced the battery of the radio"
												MsgTimer = 70 * 5		
										End Select 
									Default
										Msg = "This item can't be used this way"
										MsgTimer = 70 * 5	
								End Select
							Default
								Msg = "This item can't be used this way"
								MsgTimer = 70 * 5
						End Select					
					End If
					
				End If
				SelectedItem = Null
			End If
		End If
		
		If Fullscreen Then DrawImage CursorIMG, MouseX(),MouseY()
		
		If InvOpen = False Then ResumeSounds()
		
	Else ;invopen = False
		
		If SelectedItem <> Null Then
			Select SelectedItem\itemtemplate\tempname
				Case "battery"
					;InvOpen = True
				Case "key1", "key2", "key3", "key4", "key5", "key6", "keyomni"
					DrawImage(SelectedItem\itemtemplate\invimg, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
				Case "scp500"
					If Injuries > 0 And Infect > 0 And Bloodloss > 0 Then
						Msg = "You feel all your wounds and sicknesses heal"
					ElseIf Injuries > 0 And Bloodloss > 0
						Msg = "You feel all your wounds heal"
					Else
						Msg = "You feel better"	
					EndIf
					MsgTimer = 70*7
					
					Injuries = 0
					Bloodloss = 0
					Infect = 0
					Stamina = 100
					For i = 0 To 5
						SCP1025state[i]=0
					Next
				Case "veryfinefirstaid"
					Select Rand(5)
						Case 1
							Injuries = 3.5
							Msg = "You started bleeding heavily"
							MsgTimer = 70*7
						Case 2
							Injuries = 0
							Bloodloss = 0
							Msg = "Your wounds started healing up rapidly"
							MsgTimer = 70*7
						Case 3
							Injuries = Max(0, Injuries - Rnd(0.5,3.5))
							Bloodloss = Max(0, Bloodloss - Rnd(10,100))
							Msg = "You feel much better"
							MsgTimer = 70*7
						Case 4
							BlurTimer = 10000
							Bloodloss = 0
							Msg = "You feel nauseated"
							MsgTimer = 70*7
						Case 5
							BlinkTimer = -10
							For r.Rooms = Each Rooms
								If r\RoomTemplate\Name = "pocketdimension" Then
									PositionEntity(Collider, EntityX(r\obj),0.8,EntityZ(r\obj))		
									ResetEntity Collider									
									UpdateDoors()
									UpdateRooms()
									PlaySound(Use914SFX)
									DropSpeed = 0
									Curr106\State = -2500
									Exit
								EndIf
							Next
							Msg = "You got a sudden headache"
							MsgTimer = 70*8
					End Select
					
					RemoveItem(SelectedItem)
				Case "firstaid", "finefirstaid", "firstaid2"
					If Bloodloss = 0 And Injuries = 0 Then
						Msg = "You don't need to use the kit now"
						MsgTimer = 70*5
						SelectedItem = Null
					Else
						CurrSpeed = CurveValue(0, CurrSpeed, 5.0)
						Crouch = True
						
						DrawImage(SelectedItem\itemtemplate\invimg, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
						
						width% = 300
						height% = 20
						x% = GraphicWidth / 2 - width / 2
						y% = GraphicHeight / 2 + 80
						Rect(x, y, width+4, height, False)
						For  i% = 1 To Int((width - 2) * (SelectedItem\state / 100.0) / 10)
							DrawImage(BlinkMeterIMG, x + 3 + 10 * (i - 1), y + 3)
						Next
						
						SelectedItem\state = Min(SelectedItem\state+(FPSfactor/5.0),100)			
						
						If SelectedItem\state = 100 Then
							If SelectedItem\itemtemplate\tempname = "finefirstaid" Then
								Bloodloss = 0
								Injuries = Max(0, Injuries - 2.0)
								If Injuries = 0 Then
									Msg = "You bandaged the wounds and took a painkiller. You feel fine."
								ElseIf Injuries > 1.0
									Msg = "You bandaged the wounds and took a painkiller, but you're still bleeding slightly."
								Else
									Msg = "You bandaged the wounds and took a painkiller, but you're still feeling sore."
								EndIf
								MsgTimer = 70*5
								RemoveItem(SelectedItem)
							Else
								Bloodloss = Max(0, Bloodloss - Rand(10,20))
								If Injuries => 2.5 Then
									Msg = "The wounds were way too severe to staunch the bleeding completely."
									Injuries = Max(2.5, Injuries-Rnd(0.3,0.7))
								ElseIf Injuries > 1.0
									Injuries = Max(0.5, Injuries-Rnd(0.5,1.0))
									If Injuries > 1.0 Then
										Msg = "You bandaged the wounds but were unable to staunch the bleeding completely."
									Else
										Msg = "You managed to stop the bleeding."
									EndIf
								Else
									If Injuries > 0.5 Then
										Injuries = 0.5
										Msg = "You took a painkiller. It eased the pain slightly."
									Else
										Injuries = 0.5
										Msg = "You took a painkiller, but it's still painful to walk."
									EndIf
								EndIf
								
								If SelectedItem\itemtemplate\name = "firstaid2" Then 
									Select Rand(12)
										Case 1
											SuperMan = True
											Msg = "You feel strange."
										Case 2
											InvertMouse = (Not InvertMouse)
											Msg = "You feel strange."
										Case 3
											BlurTimer = 5000
										Case 4
											EyeSuper = 70*Rand(20,30)
										Case 5
											Bloodloss = 0
											Injuries = 0
											Msg = "You bandaged the wounds. The bleeding stopped completely and you're feeling fine."
										Case 6
											Msg = "You bandaged the wounds and blood started pouring heavily through the bandages."
											Injuries = 3.5
									End Select
								EndIf
								
								MsgTimer = 70*5
								RemoveItem(SelectedItem)
							EndIf							
						EndIf
						
					EndIf
				Case "eyedrops"
					If (Not Wearing714) Then EyeSuper = 70*Rand(20,30)
					BlurTimer = 200
					RemoveItem(SelectedItem)
				Case "fineeyedrops"
					If (Not Wearing714) Then 
						EyeSuper = 70*Rand(30,40)
						Bloodloss = Max(Bloodloss-1.0, 0)
					EndIf
					BlurTimer = 200
					RemoveItem(SelectedItem)
				Case "supereyedrops"
					If (Not Wearing714) Then 
						EyeSuper = 70*1000
						EyeStuck = 10000
					EndIf
					BlurTimer = 1000
					RemoveItem(SelectedItem)					
				Case "paper"
					If SelectedItem\itemtemplate\img=0 Then
						If SelectedItem\itemtemplate\name = "Burnt Note" Then
							SelectedItem\itemtemplate\img = LoadImage("GFX\items\bn.it")
							DebugLog "accesscode: "+AccessCode
							SetBuffer ImageBuffer(SelectedItem\itemtemplate\img)
							Color 0,0,0
							Text 277, 469, AccessCode, True, True
							Color 255,255,255
							SetBuffer BackBuffer()
						Else
							SelectedItem\itemtemplate\img=LoadImage(SelectedItem\itemtemplate\imgpath)	
							
							ResizeImage(SelectedItem\itemtemplate\img, ImageWidth(SelectedItem\itemtemplate\img) * MenuScale, ImageHeight(SelectedItem\itemtemplate\img) * MenuScale)
						EndIf
						MaskImage(SelectedItem\itemtemplate\img, 255, 0, 255)
					EndIf
					
					DrawImage(SelectedItem\itemtemplate\img, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\img) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\img) / 2)
				Case "scp1025"
					Achievements(Achv1025)=True 
					If SelectedItem\itemtemplate\img=0 Then
						SelectedItem\state = Rand(0,5)
						SelectedItem\itemtemplate\img=LoadImage("GFX\items\1025\1025_"+Int(SelectedItem\state)+".jpg")	
						ResizeImage(SelectedItem\itemtemplate\img, ImageWidth(SelectedItem\itemtemplate\img) * MenuScale, ImageHeight(SelectedItem\itemtemplate\img) * MenuScale)
						
						MaskImage(SelectedItem\itemtemplate\img, 255, 0, 255)
					EndIf
					
					SCP1025state[SelectedItem\state]=Max(1,SCP1025state[SelectedItem\state])					
					
					DrawImage(SelectedItem\itemtemplate\img, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\img) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\img) / 2)
					
				Case "radio","18vradio","fineradio","veryfineradio"
					If SelectedItem\state <= 100 Then SelectedItem\state = Max(0, SelectedItem\state - FPSfactor * 0.01)
					
					If SelectedItem\itemtemplate\img=0 Then
						SelectedItem\itemtemplate\img=LoadImage(SelectedItem\itemtemplate\imgpath)	
						MaskImage(SelectedItem\itemtemplate\img, 255, 0, 255)
					EndIf
					
					;radiostate(5) = onko n‰ytetty jo ohjeteksti
					;radiostate(6) = "koodikanavan" ajastin
					;RadioState(7) = "koodikanavan" kakkosajastin
					
					;radiostate(8) = onko l‰hetetty jo "manual door control disabled" -viesti
					If RadioState(5) = 0 Then 
						Msg = "Use keys 1-5 to change the channel"
						MsgTimer = 70 * 5
						RadioState(5) = 1
					EndIf
					
					Local strtemp$ = ""
					
					x = GraphicWidth - ImageWidth(SelectedItem\itemtemplate\img) + 120
					y = GraphicHeight - ImageHeight(SelectedItem\itemtemplate\img) - 30
					
					DrawImage(SelectedItem\itemtemplate\img, x, y)
					
					If SelectedItem\state > 0 Then 
						
						If PlayerRoom\RoomTemplate\Name = "pocketdimension" Or CoffinDistance < 4.0 Then
							ResumeChannel(RadioCHN(0))
							If ChannelPlaying(RadioCHN(0)) = False Then RadioCHN(0) = PlaySound(RadioStatic)	
						Else
							Select Int(SelectedItem\state2)
								Case 0 ;randomkanava
									ResumeChannel(RadioCHN(0))
									If ChannelPlaying(RadioCHN(0)) = False Then RadioCHN(0) = PlaySound(RadioStatic)
								Case 1 ;h‰lytyskanava
									DebugLog RadioState(1) 
									
									ResumeChannel(RadioCHN(1))
									strtemp = "        WARNING - CONTAINMENT BREACH          "
									If ChannelPlaying(RadioCHN(1)) = False Then
										
										If RadioState(1) => 5 Then
											RadioCHN(1) = PlaySound(RadioSFX(1,1))	
											RadioState(1) = 0
										Else
											RadioState(1)=RadioState(1)+1	
											RadioCHN(1) = PlaySound(RadioSFX(1,0))	
										EndIf
										
									EndIf
									
								Case 2 ;scp-radio
									ResumeChannel(RadioCHN(2))
									strtemp = "        SCP Foundation On-Site Radio          "
									If ChannelPlaying(RadioCHN(2)) = False Then
										DebugLog "bim"  
										RadioState(2)=RadioState(2)+1
										If RadioState(2) = 17 Then RadioState(2) = 1
										If Floor(RadioState(2)/2)=Ceil(RadioState(2)/2) Then ;parillinen, soitetaan normiviesti
											RadioCHN(2) = PlaySound(RadioSFX(2,Int(RadioState(2)/2)))	
										Else ;pariton, soitetaan musiikkia
											RadioCHN(2) = PlaySound(RadioSFX(2,0))
										EndIf
									EndIf 
								Case 3
									ResumeChannel(RadioCHN(3))
									strtemp = "EMERGENCY CHANNEL - RESERVED FOR COMMUNICATION IN THE EVENT OF A CONTAINMENT BREACH"
									If ChannelPlaying(RadioCHN(3)) = False Then RadioCHN(3) = PlaySound(RadioStatic)
									
									If MTFtimer > 0 Then 
										RadioState(3)=RadioState(3)+Max(Rand(-10,1),0)
										Select RadioState(3)
											Case 40
												TempSound = LoadSound("SFX\MTF\Random1.ogg")
												RadioCHN(3) = PlaySound(TempSound)
												RadioState(3)=RadioState(3)+1													
											Case 400
												TempSound = LoadSound("SFX\MTF\Random2.ogg")
												RadioCHN(3) = PlaySound(TempSound)
												RadioState(3)=RadioState(3)+1	
											Case 800
												TempSound = LoadSound("SFX\MTF\Random3.ogg")
												RadioCHN(3) = PlaySound(TempSound)
												RadioState(3)=RadioState(3)+1															
											Case 1200
												TempSound = LoadSound("SFX\MTF\Random4.ogg")
												RadioCHN(3) = PlaySound(TempSound)	
												RadioState(3)=RadioState(3)+1		
										End Select
									EndIf
								Case 4
									ResumeChannel(RadioCHN(6)) ;taustalle kohinaa
									If ChannelPlaying(RadioCHN(6)) = False Then RadioCHN(6) = PlaySound(RadioStatic)									
									
									ResumeChannel(RadioCHN(4))
									If ChannelPlaying(RadioCHN(4)) = False Then 
										If TempSound <> 0 Then FreeSound TempSound : TempSound = 0
										
										If RemoteDoorOn = False And RadioState(8) = False Then
											TempSound = LoadSound("SFX\radio\Chatter3.ogg")
											RadioCHN(4) = PlaySound(TempSound)	
											RadioState(8) = True
										Else
											RadioState(4)=RadioState(4)+Max(Rand(-10,1),0)
											
											Select RadioState(4)
												Case 10
													TempSound = LoadSound("SFX\radio\OhGod.ogg")
													RadioCHN(4) = PlaySound(TempSound)
													RadioState(4)=RadioState(4)+1													
												Case 100
													TempSound = LoadSound("SFX\radio\Chatter2.ogg")
													RadioCHN(4) = PlaySound(TempSound)
													RadioState(4)=RadioState(4)+1	
												Case 158
													TempSound = ("SFX\radio\Chatter4.ogg")
													RadioCHN(4) = PlaySound(TempSound)
													RadioState(4)=RadioState(4)+1															
												Case 200
													TempSound = LoadSound("SFX\radio\Chatter1.ogg")
													RadioCHN(4) = PlaySound(TempSound)	
													RadioState(4)=RadioState(4)+1		
											End Select
										EndIf
									EndIf
									
									
								Case 5
									ResumeChannel(RadioCHN(5))
									If ChannelPlaying(RadioCHN(5)) = False Then RadioCHN(5) = PlaySound(RadioStatic)
							End Select 
							
							;If KeyHit(25) Then StopChannel RadioCHN(2)
							
							Color (230, 223, 204)
							Text(x+195, y+269, "CHN")						
							
							If SelectedItem\itemtemplate\tempname = "veryfineradio" Then ;"KOODIKANAVA"
								ResumeChannel(RadioCHN(0))
								If ChannelPlaying(RadioCHN(0)) = False Then RadioCHN(0) = PlaySound(RadioStatic)
								
								;radiostate(7)=kuinka mones piippaus menossa
								;radiostate(8)=kuinka mones access coden numero menossa
								RadioState(6)=RadioState(6) + FPSfactor
								temp = Mid(Str(AccessCode),RadioState(8)+1,1)
								If RadioState(6)-FPSfactor =< RadioState(7)*50 And RadioState(6)>RadioState(7)*50 Then
									PlaySound(RadioBuzz)
									RadioState(7)=RadioState(7)+1
									If RadioState(7)=>temp Then
										RadioState(7)=0
										RadioState(6)=-100
										RadioState(8)=RadioState(8)+1
										If RadioState(8)=4 Then RadioState(8)=0 : RadioState(6)=-200
									EndIf
								EndIf
								
								SetFont Font2
								Text(x+214, y+310, Rand(0,9),True,True)	
								
							Else
								For i = 2 To 6
									If KeyHit(i) Then
										If SelectedItem\state2 <> i-2 Then ;pausetetaan nykyinen radiokanava
											PlaySound RadioSquelch
											If RadioCHN(Int(SelectedItem\state2)) <> 0 Then PauseChannel(RadioCHN(Int(SelectedItem\state2)))
										EndIf
										SelectedItem\state2 = i-2
										;jos nykyist‰ kanavaa ollaan soitettu, laitetaan jatketaan toistoa samasta kohdasta
										If RadioCHN(SelectedItem\state2)<>0 Then ResumeChannel(RadioCHN(SelectedItem\state2))
									EndIf
								Next
								
								SetFont Font2
								Text(x+214, y+310, Int(SelectedItem\state2+1),True,True)
							EndIf
							
							SetFont Font1
							
							If strtemp <> "" Then 
								Text(x+250, y+325, Right(Left(strtemp, (Int(MilliSecs()/500) Mod Len(strtemp))),7))
							EndIf
							
							x = x+321
							y= y+264
							If SelectedItem\state <= 100 Then
							;Text (x - 60, y - 20, "BATTERY")
								For i = 1 To 5
									If Ceil(SelectedItem\state / 20.0) > 5 - i Then
										Rect(x - (40 - i * 6), y, 40 - i * 6, 5)
									Else
										Rect(x - (40 - i * 6), y, 40 - i * 6, 5, False)
									EndIf
									
									y=y+10
								Next
							EndIf	
						EndIf
						
					EndIf
					
				Case "cigarette"
					Msg = "I don't have anything to light it with. Umm, what about that... Nevermind."
					MsgTimer = 70 * 5
					RemoveItem(SelectedItem)
				Case "420"
					If Wearing714 Then
						Msg = "DUDE WTF THIS SHIT DOESN'T EVEN WORK"	
					Else
						Msg = "MAN DATS SUM GOOD ASS SHIT"
						Injuries = Max(Injuries-0.5, 0)
						BlurTimer = 500
						Achievements(Achv420) = True
						TempSound = LoadSound("SFX\Mandeville.ogg")
						PlaySound TempSound						
					EndIf
					MsgTimer = 70 * 5
					RemoveItem(SelectedItem)
				Case "420s"
					If Wearing714 Then
						Msg = "DUDE WTF THIS SHIT DOESN'T EVEN WORK"	
					Else
						Msg = "UH WHERE... WHAT WAS I DOING AGAIN... MAN I NEED TO TAKE A NAP..."
						KillTimer = -1						
					EndIf
					MsgTimer = 70 * 6
					RemoveItem(SelectedItem)
				Case "scp714"
					If Wearing714 Then
						Msg = "You took off the ring."
						Wearing714 = False
					Else
						Achievements(Achv714)=True
						Msg = "You put on the ring."
						Wearing714 = True
					EndIf
					MsgTimer = 70 * 5
					SelectedItem = Null	
				Case "hazmatsuit", "hazmatsuit2"
					If WearingHazmat Then
						Msg = "You take off the hazmat suit."
					Else
						Msg = "You put on the hazmat suit."
					EndIf
					MsgTimer = 70 * 5
					If SelectedItem\itemtemplate\tempname="hazmatsuit2" Then
						If WearingHazmat=0 Then WearingHazmat = 2 Else WearingHazmat=0
					Else
						WearingHazmat = (Not WearingHazmat)
					EndIf
					SelectedItem = Null	
				Case "vest"
					If WearingVest Then
						Msg = "You took off the vest."
						WearingVest = False
					Else
						Msg = "You put on the vest and feel slightly encumbered."
						WearingVest = True
					EndIf
					MsgTimer = 70 * 7
					SelectedItem = Null
				Case "finevest"
					If WearingVest Then
						Msg = "You took off the vest."
						WearingVest = False						
					Else
						Msg = "You put on the vest and feel heavily encumbered."
						WearingVest = 2
					EndIf
					SelectedItem = Null			
				Case "gasmask"
					If WearingGasMask Then
						Msg = "You took off the gas mask."
					Else
						Msg = "You put on the gas mask."
					EndIf
					MsgTimer = 70 * 5
					WearingGasMask = Not WearingGasMask
					SelectedItem = Null
				Case "supergasmask"
					If WearingGasMask Then
						Msg = "You took off the gas mask."
					Else
						Msg = "You put on the gas mask. It feels as if it was easier to breath than normally."
					EndIf
					MsgTimer = 70 * 8
					If WearingGasMask Then WearingGasMask = 0 Else WearingGasMask = 2
					SelectedItem = Null					
				Case "navigator", "nav"
					
					If SelectedItem\itemtemplate\img=0 Then
						SelectedItem\itemtemplate\img=LoadImage(SelectedItem\itemtemplate\imgpath)	
						MaskImage(SelectedItem\itemtemplate\img, 255, 0, 255)
					EndIf
					
					If SelectedItem\state <= 100 Then SelectedItem\state = Max(0, SelectedItem\state - FPSfactor * 0.01)
					
					x = GraphicWidth / 2 + 7
					y = GraphicHeight / 2 - 28
					width = 287
					height = 256
					
					DrawImage(SelectedItem\itemtemplate\img, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\img) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\img) / 2)
					
					If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then
						If (MilliSecs() Mod 1000) > 300 Then	
							Text(x, y + height / 2 - 80, "ERROR 06", True)
							Text(x, y + height / 2 - 60, "LOCATION UNKNOWN", True)						
						EndIf
					Else
						
						If SelectedItem\state > 0 And (Rnd(CoffinDistance + 15.0) > 1.0 Or PlayerRoom\RoomTemplate\Name <> "coffin") Then
							
							Color (230, 223, 204)
							If SelectedItem\itemtemplate\name = "S-NAV Navigator" Then Color(255, 0, 0)
							If (MilliSecs() Mod 1000) > 300 Then
								If SelectedItem\itemtemplate\name <> "S-NAV 310 Navigator" And SelectedItem\itemtemplate\name <> "S-NAV Navigator Ultimate" Then
									Text(x, y + height / 2 - 40, "COULDN'T CONNECT", True)
									Text(x, y + height / 2 - 20, "TO MAP DATABASE", True)
								EndIf
								Rect(x - 4, y - 4 - 7, 8, 8, False)
							EndIf
							
							Local PlayerX% = Floor(EntityX(PlayerRoom\obj) / 8.0 + 0.5), PlayerZ% = Floor(EntityZ(PlayerRoom\obj) / 8.0 + 0.5)
							If SelectedItem\itemtemplate\name = "S-NAV Navigator Ultimate" And (MilliSecs() Mod 600) < 400 Then
								Local dist# = EntityDistance(Camera, Curr173\obj)
								dist = Ceil(dist / 8.0) * 8.0
								If dist < 8.0 * 4 Then
									Color 255, 0, 0
									Oval(x - dist * 3, y - 7 - dist * 3, dist * 3 * 2, dist * 3 * 2, False)
									Text(x - width / 2 + 20, y - height / 2 + 20, "SCP-173")
								EndIf
								dist# = EntityDistance(Camera, Curr106\obj)
								If dist < 8.0 * 4 Then
									Color 255, 0, 0
									Oval(x - dist * 1.5, y - 7 - dist * 1.5, dist * 3, dist * 3, False)
									Text(x - width / 2 + 20, y - height / 2 + 40, "SCP-106")
								EndIf
								If PlayerRoom\RoomTemplate\Name = "coffin" Then
									If CoffinDistance < 8.0 Then
										dist = Rnd(4.0, 8.0)
										Color 255, 0, 0
										Oval(x - dist * 1.5, y - 7 - dist * 1.5, dist * 3, dist * 3, False)
										Text(x - width / 2 + 20, y - height / 2 + 40, "SCP-895")
									EndIf
								EndIf
								For np.npcs = Each NPCs
									If np\npctype = NPCtype096 Then
										dist# = EntityDistance(Camera, np\obj)
										If dist < 8.0 * 4 Then
											Color 255, 0, 0
											Oval(x - dist * 1.5, y - 7 - dist * 1.5, dist * 3, dist * 3, False)
											Text(x - width / 2 + 20, y - height / 2 + 40, "SCP-096")
										EndIf
										Exit
									EndIf
								Next
							End If
							
							dist = 4
							Local x2%, z2%
							For x2 = Max(1, PlayerX - dist) To Min(MapWidth - 2, PlayerX + dist)
								For z2 = Max(1, PlayerZ - dist) To Min(MapHeight - 2, PlayerZ + dist)
									
									If MapTemp(PlayerLevel, x2, z2) And (MapFound(PlayerLevel, x2, z2) > 0 Or SelectedItem\itemtemplate\name = "S-NAV 310 Navigator" Or SelectedItem\itemtemplate\name = "S-NAV Navigator Ultimate") Then
										Local drawx% = x + (PlayerX - x2) * 24, drawy% = y - (PlayerZ - z2) * 24 - 7
										If PlayerRoom\RoomTemplate\Name = "coffin" Then
											If CoffinDistance < 8.0 And Rnd(CoffinDistance) < 0.5 Then
												drawx = drawx + (15.0 - CoffinDistance) * Rnd(-1, 1)
												drawy = drawy + (15.0 - CoffinDistance) * Rnd(-1, 1)
											End If
										EndIf										
										
										If MapFound(PlayerLevel, x2, z2) = 1 Then
											Color (230 * 0.5, 223 * 0.5, 204 * 0.5)
											If SelectedItem\itemtemplate\name = "S-NAV Navigator" Then Color(255 * 0.5, 0, 0)
										Else
											Color (230, 223, 204)
											If SelectedItem\itemtemplate\name = "S-NAV Navigator" Then Color(255, 0, 0)
										EndIf
										;rect(x + (PlayerX - x2) * 25 - 12, y - (PlayerZ - z2) * 25 - 12, 25, 25, False)
										
										If MapTemp(PlayerLevel, x2 + 1, z2) = False Then Line(drawx - 12, drawy - 12, drawx - 12, drawy + 12)
										If MapTemp(PlayerLevel, x2 - 1, z2) = False Then Line(drawx + 12, drawy - 12, drawx + 12, drawy + 12)
										
										If MapTemp(PlayerLevel, x2, z2 - 1) = False Then Line(drawx - 12, drawy - 12, drawx + 12, drawy - 12)
										If MapTemp(PlayerLevel, x2, z2 + 1)= False Then Line(drawx - 12, drawy + 12, drawx + 12, drawy + 12)
										
										
									End If
									
								Next
							Next
							
							x = GraphicWidth / 2 + width / 2 - 10
							y = GraphicHeight / 2 - height / 2
							Color (230, 223, 204)
							If SelectedItem\itemtemplate\name = "S-NAV Navigator" Then Color(255, 0, 0)
							If SelectedItem\state <= 100 Then
								Text (x - 60, y - 20, "BATTERY")
								For i = 1 To 5
									If Ceil(SelectedItem\state / 20.0) > 5 - i Then
										Rect(x - (40 - i * 6), y, 40 - i * 6, 5)
									Else
										Rect(x - (40 - i * 6), y, 40 - i * 6, 5, False)
									EndIf
									
									y=y+15
								Next
							EndIf
							
						EndIf
						
					EndIf
					
			End Select
			
			If MouseHit2 Then
				EntityAlpha Dark, 0.0
				
				If SelectedItem\itemtemplate\tempname = "paper" Or SelectedItem\itemtemplate\tempname = "scp1025"  Then
					If SelectedItem\itemtemplate\img<>0 Then FreeImage(SelectedItem\itemtemplate\img)
					SelectedItem\itemtemplate\img=0
				EndIf
				
				If SelectedItem\itemtemplate\sound <> 66 Then PlaySound(PickSFX(SelectedItem\itemtemplate\sound))
				SelectedItem = Null
			EndIf
		End If		
	EndIf
	
	If SelectedItem = Null Then
		For i = 0 To 6
			If RadioCHN(i) <> 0 Then 
				If ChannelPlaying(RadioCHN(i)) Then PauseChannel(RadioCHN(i))
			EndIf
		Next
	EndIf 
	
	If PrevInvOpen And (Not InvOpen) Then MoveMouse viewport_center_x, viewport_center_y
End Function

Function DrawMenu()
	Local x%, y%, width%, height%
	
	If MenuOpen Then
		
		InvOpen = False
		
		width = ImageWidth(PauseMenuIMG)
		height = ImageHeight(PauseMenuIMG)
		x = GraphicWidth / 2 - width / 2
		y = GraphicHeight / 2 - height / 2
		
		DrawImage PauseMenuIMG, x, y
		
		Color(255, 255, 255)
		
		x = x+132
		y = y+122	
		
		Text x, y, "Designation: D-9341"
		Text x, y+20, "Name: [REDACTED]"
		
		Text x, y+50,	"Save: "+CurrSave
		Text x, y+70, "Map seed: "+RandomSeed
		
		x=x-132
		y=y-122
		
		x=x+49
		y=y-5		
		If KillTimer >= 0 Then
			SetFont Font2
			Text(x + width / 2, y + 20, "PAUSED", True)
			SetFont Font1
		Else
			SetFont Font2
			Text(x + width / 2, y + 20, "YOU DIED", True)
			SetFont Font1
		End If
		
		x=x-77
		y=y+130
		
		If SelectedMode = 0 Then ;euclid-mode
			If KillTimer >= 0 Then ;ei ole kuollut			
				If DrawButton(x + width / 2 - 150, y + 154, 300, 60, "Resume") Then MenuOpen = False
				
				If PlayerRoom\RoomTemplate\Name <> "173" And PlayerRoom\RoomTemplate\Name <> "exit1" Then
					If DrawButton(x + width / 2 - 150, y + 154 + 80, 300, 60, "Save & quit") Then
						SaveGame(SavePath + CurrSave + "\")
						NullGame()
						MenuOpen = False
						MainMenuOpen = True
						MainMenuTab = 0
						CurrSave = ""
						FlushKeys()	
					EndIf
				Else
					DrawButton(x + width / 2 - 150, y + 154 + 80, 300, 60, "")
					Color 50,50,50
					Text(x + width / 2, y + 154 + 80 + 30, "Save & quit", True, True)
				EndIf
				If DrawButton(x + width / 2 - 150, y + 154 + 160, 300, 60, "Quit") Then
					NullGame()
					MenuOpen = False
					MainMenuOpen = True
					MainMenuTab = 0
					CurrSave = ""
					FlushKeys()
				EndIf							
			Else ;kuoli
				If GameSaved Then
					If DrawButton(x + width / 2 - 150, y + 154, 300, 60, "Load game") Then
						;NullGame()
						DrawLoading(0)
						
						MenuOpen = False
						LoadGameQuick(SavePath + CurrSave + "\")
						;LoadEntities()
						;LoadGame(SavePath + CurrSave + "\")
						;InitLoadGame()
						
						MoveMouse 320, 240
						SetFont Font1
						HidePointer ()
						
						FlushKeys()
						FlushMouse()
						
						
						DrawLoading(100)
						
						PrevTime = MilliSecs()
						FPSfactor = 0	
					EndIf
				Else
					DrawButton(x + width / 2 - 150, y + 154, 300, 60, "")
					Color 50,50,50
					Text(x + width / 2, y + 154 + 30, "Load game", True, True)
				EndIf
				If DrawButton(x + width / 2 - 150, y + 154 + 80, 300, 60, "Quit") Then
					NullGame()
					MenuOpen = False
					MainMenuOpen = True
					MainMenuTab = 0
					CurrSave = ""
					FlushKeys()
				EndIf				
			End If
			
		Else ;keter-mode
			
			If KillTimer >= 0 Then ;ei ole kuollut
				If DrawButton(x + width / 2 - 150, y + 154, 300, 60, "Resume") Then MenuOpen = False
				If PlayerRoom\RoomTemplate\Name <> "173" Then
					If DrawButton(x + width / 2 - 150, y + 154 + 80, 300, 60, "Save & quit") Then
						SaveGame(SavePath + CurrSave + "\")
						NullGame()
						MenuOpen = False
						MainMenuOpen = True
						MainMenuTab = 0
						CurrSave = ""
						FlushKeys()
					EndIf
				Else
					DrawButton(x + width / 2 - 150, y + 154 + 80, 300, 60, "")
					Color 50,50,50
					Text(x + width / 2, y + 154 + 80 + 30, "Save & quit", True, True)
				EndIf
				
				If DrawButton(x + width / 2 - 150, y + 154 + 160, 300, 60, "Quit") Then
					NullGame()
					MenuOpen = False
					MainMenuOpen = True
					MainMenuTab = 0
					CurrSave = ""
					FlushKeys()
				EndIf	
			Else ;kuoli, save poistettu
				If DrawButton(x + width / 2 - 150, y + 154, 300, 60, "Quit") Then
					NullGame()
					MenuOpen = False
					MainMenuOpen = True
					MainMenuTab = 0
					CurrSave = ""
					FlushKeys()	
				EndIf
			EndIf
			
		End If
		
		If Fullscreen Then DrawImage CursorIMG, MouseX(),MouseY()
	
	End If
	
	SetFont Font1
End Function

Function MouseOn%(x%, y%, width%, height%)
	If MouseX() > x And MouseX() < x + width Then
		If MouseY() > y And MouseY() < y + height Then
			Return True
		End If
	End If
	Return False
End Function

;----------------------------------------------------------------------------------------------

Function LoadEntities()
	DrawLoading(0)
	
	Local i%
	
	SoundEmitter = CreatePivot()
	
	Camera = CreateCamera()
	CameraRange(Camera, 0.05, 16)
	CameraFogMode (Camera, 1)
	CameraFogRange (Camera, CameraFogNear, CameraFogFar)
	CameraFogColor (Camera, GetINIInt("options.ini", "options", "fog r"), GetINIInt("options.ini", "options", "fog g"), GetINIInt("options.ini", "options", "fog b"))
	AmbientLight Brightness, Brightness, Brightness
	
	CreateBlurImage()
	;Listener = CreateListener(Camera)
	
	FogTexture = LoadTexture("GFX\fog.jpg", 1)
	
	Fog = CreateSprite(Camera)
	ScaleSprite(Fog, Max(GraphicWidth / 1240.0, 1.0), Max(GraphicHeight / 960.0 * 0.8, 0.8))
	EntityTexture(Fog, FogTexture)
	EntityBlend (Fog, 2)
	EntityOrder Fog, -1000
	MoveEntity(Fog, 0, 0, 1.0)
	
	GasMaskTexture = LoadTexture("GFX\GasmaskOverlay.jpg", 1)
	GasMaskOverlay = CreateSprite(Camera)
	ScaleSprite(GasMaskOverlay, Max(GraphicWidth / 1024.0, 1.0), Max(GraphicHeight / 1024.0 * 0.8, 0.8))
	EntityTexture(GasMaskOverlay, GasMaskTexture)
	EntityBlend (GasMaskOverlay, 2)
	EntityFX(GasMaskOverlay, 1)
	EntityOrder GasMaskOverlay, -1003
	MoveEntity(GasMaskOverlay, 0, 0, 1.0)
	HideEntity(GasMaskOverlay)
	
	InfectTexture = LoadTexture("GFX\InfectOverlay.jpg", 1)
	InfectOverlay = CreateSprite(Camera)
	ScaleSprite(InfectOverlay, Max(GraphicWidth / 1024.0, 1.0), Max(GraphicHeight / 1024.0 * 0.8, 0.8))
	EntityTexture(InfectOverlay, InfectTexture)
	EntityBlend (InfectOverlay, 3)
	EntityFX(InfectOverlay, 1)
	EntityOrder InfectOverlay, -1003
	MoveEntity(InfectOverlay, 0, 0, 1.0)
	;EntityAlpha (InfectOverlay, 255.0)
	HideEntity(InfectOverlay)
	
	DrawLoading(5)
	
	DarkTexture = CreateTexture(1024, 1024, 1 + 2)
	SetBuffer TextureBuffer(DarkTexture)
	Cls
	SetBuffer BackBuffer()
	
	Dark = CreateSprite(Camera)
	ScaleSprite(Dark, Max(GraphicWidth / 1240.0, 1.0), Max(GraphicHeight / 960.0 * 0.8, 0.8))
	EntityTexture(Dark, DarkTexture)
	EntityBlend (Dark, 1)
	EntityOrder Dark, -1002
	MoveEntity(Dark, 0, 0, 1.0)
	EntityAlpha Dark, 0.0
	
	LightTexture = CreateTexture(1024, 1024, 1 + 2)
	SetBuffer TextureBuffer(LightTexture)
	ClsColor 255, 255, 255
	Cls
	ClsColor 0, 0, 0
	SetBuffer BackBuffer()
	
	TeslaTexture = LoadTexture("GFX\map\tesla.jpg", 1+2)
	
	Light = CreateSprite(Camera)
	ScaleSprite(Light, Max(GraphicWidth / 1240.0, 1.0), Max(GraphicHeight / 960.0 * 0.8, 0.8))
	EntityTexture(Light, LightTexture)
	EntityBlend (Light, 1)
	EntityOrder Light, -1002
	MoveEntity(Light, 0, 0, 1.0)
	HideEntity Light
	
	Collider = CreatePivot()
	EntityRadius Collider, 0.15, 0.30
	EntityPickMode(Collider, 1)
	EntityType Collider, HIT_PLAYER
	
	Head = CreatePivot()
	EntityRadius Head, 0.15
	EntityType Head, HIT_PLAYER
	
	;SignObj = LoadMesh("GFX\map\signs\sign.b3d")
	
	LightSpriteTex(0) = LoadTexture("GFX\light1.jpg", 1)
	LightSpriteTex(1) = LoadTexture("GFX\light2.jpg", 1)
	
	DrawLoading(10)
	
	DoorOBJ = LoadMesh("GFX\map\door01.x")
	HideEntity DoorOBJ
	DoorFrameOBJ = LoadMesh("GFX\map\doorframe.x")
	HideEntity DoorFrameOBJ
	
	HeavyDoorObj(0) = LoadMesh("GFX\map\heavydoor1.x")
	HideEntity HeavyDoorObj(0)
	HeavyDoorObj(1) = LoadMesh("GFX\map\heavydoor2.x")
	HideEntity HeavyDoorObj(1)
	
	DoorColl = LoadMesh("GFX\map\doorcoll.x")
	HideEntity DoorColl
	
	ButtonOBJ = LoadMesh("GFX\map\Button.x")
	HideEntity ButtonOBJ
	ButtonKeyOBJ = LoadMesh("GFX\map\ButtonKeycard.x")
	HideEntity ButtonKeyOBJ
	ButtonCodeOBJ = LoadMesh("GFX\map\ButtonCode.x")
	HideEntity ButtonCodeOBJ	
	
	BigDoorOBJ(0) = LoadMesh("GFX\map\ContDoorLeft.x")
	HideEntity BigDoorOBJ(0)
	BigDoorOBJ(1) = LoadMesh("GFX\map\ContDoorRight.x")
	HideEntity BigDoorOBJ(1)
	
	LeverBaseOBJ = LoadMesh("GFX\map\leverbase.x")
	LeverOBJ = LoadMesh("GFX\map\leverhandle.x")
	
	For i = 0 To 1
		HideEntity BigDoorOBJ(i)
		If BumpEnabled Then 
			Local bumptex = LoadTexture("GFX\map\containmentdoorsbump.jpg")
			TextureBlend bumptex, FE_BUMP
			Local tex = LoadTexture("GFX\map\containment_doors.jpg")	
			EntityTexture BigDoorOBJ(i), bumptex, 0, 0
			EntityTexture BigDoorOBJ(i), tex, 0, 1
		EndIf
	Next
	
	DrawLoading(15)
	
	;BigDoorFrameOBJ = loadMesh("GFX\map\bigdoorframe.x")
	;hideEntity BigDoorFrameOBJ
	
	For i = 0 To 6
		GorePics(i) = LoadTexture("GFX\895pics\pic" + (i + 1) + ".jpg")
	Next
	
	OldAiPics(0) = LoadTexture("GFX\AIface.jpg")
	OldAiPics(1) = LoadTexture("GFX\AIface2.jpg")	
	
	DrawLoading(20)
	
	For i = 0 To 6
		DecalTextures(i) = LoadTexture("GFX\decal" + (i + 1) + ".png", 1 + 2)
	Next
	DecalTextures(7) = LoadTexture("GFX\items\INVpaperstrips.jpg", 1 + 2)
	For i = 8 To 12
		DecalTextures(i) = LoadTexture("GFX\decalpd"+(i-7)+".jpg", 1 + 2)	
	Next
	For i = 13 To 14
		DecalTextures(i) = LoadTexture("GFX\bullethole"+(i-12)+".jpg", 1 + 2)	
	Next	
	For i = 15 To 16
		DecalTextures(i) = LoadTexture("GFX\blooddrop"+(i-14)+".png", 1 + 2)	
	Next
	DecalTextures(17) = LoadTexture("GFX\decal8.png", 1 + 2)	
	
	DrawLoading(25)
	
	Monitor = LoadMesh("GFX\map\monitor.b3d")
	HideEntity Monitor
	MonitorTexture = LoadTexture("GFX\monitortexture.jpg")
	
	CamBaseOBJ = LoadMesh("GFX\map\cambase.x")
	HideEntity(CamBaseOBJ)
	CamOBJ = LoadMesh("GFX\map\CamHead.b3d")
	HideEntity(CamOBJ)
	
	InitItemTemplates()
	
	DrawLoading(30)
	
	ParticleTextures(0) = LoadTexture("GFX\smoke.png", 1 + 2)
	ParticleTextures(1) = LoadTexture("GFX\flash.jpg", 1 + 2)
	ParticleTextures(2) = LoadTexture("GFX\dust.jpg", 1 + 2)
	ParticleTextures(3) = LoadTexture("GFX\npcs\hg.pt", 1 + 2)
	ParticleTextures(4) = LoadTexture("GFX\map\sun.jpg", 1 + 2)
	ParticleTextures(5) = LoadTexture("GFX\bloodsprite.png", 1 + 2)
	ParticleTextures(6) = LoadTexture("GFX\smoke2.png", 1 + 2)
	
	LoadMaterials("DATA\materials.ini")
	
	DrawLoading(35)
	
	LoadRoomMeshes()
	
End Function

Function InitNewGame()
	
	Local i%, de.Decals, d.Doors, it.Items, r.Rooms, sc.SecurityCams 
	
	DrawLoading(60)
	
	HideDistance# = 15.0
	
	HeartBeatRate = 70
	
	AccessCode = 0
	For i = 0 To 3
		AccessCode = AccessCode + Rand(1,9)*(10^i)
	Next	
	
	CreateMap()
	InitWayPoints()
	
	DrawLoading(79)
	
	Achievements(AchvConsole) = 1
	
	Curr173 = CreateNPC(NPCtype173, 0, -30.0, 0)
	Curr106 = CreateNPC(NPCtypeOldMan, 0, -30.0, 0)
	Curr106\State = 70 * 60 * Rand(12,17)
	
	For d.Doors = Each Doors
		EntityParent(d\obj, 0)
		If d\obj2 > 0 Then EntityParent(d\obj2, 0)
		If d\frameobj > 0 Then EntityParent(d\frameobj, 0)
		If d\buttons[0] > 0 Then EntityParent(d\buttons[0], 0)
		If d\buttons[1] > 0 Then EntityParent(d\buttons[1], 0)
		
		If d\obj2 <> 0 And d\dir = 0 Then
			MoveEntity(d\obj, 0, 0, 8.0 * RoomScale)
			MoveEntity(d\obj2, 0, 0, 8.0 * RoomScale)
		EndIf	
	Next
	
	For it.Items = Each Items
		EntityType (it\obj, HIT_ITEM)
		EntityParent(it\obj, 0)
	Next
	
	DrawLoading(80)
	For sc.SecurityCams= Each SecurityCams
		sc\angle = EntityYaw(sc\obj) + sc\angle
		EntityParent(sc\obj, 0)
	Next	
	
	For r.Rooms = Each Rooms
		For i = 0 To 19
			If r\Lights[i]<>0 Then EntityParent(r\Lights[i],0)
		Next
		
		If (Not r\RoomTemplate\DisableDecals) Then
			If Rand(4) = 1 Then
				de.Decals = CreateDecal(Rand(2, 3), EntityX(r\obj)+Rnd(- 2,2), 0.003, EntityZ(r\obj)+Rnd(-2,2), 90, Rand(360), 0)
				de\Size = Rnd(0.1, 0.4) : ScaleSprite(de\obj, de\Size, de\Size)
				EntityAlpha(de\obj, Rnd(0.85, 0.95))
			EndIf
			
			If Rand(4) = 1 Then
				de.Decals = CreateDecal(0, EntityX(r\obj)+Rnd(- 2,2), 0.003, EntityZ(r\obj)+Rnd(-2,2), 90, Rand(360), 0)
				de\Size = Rnd(0.5, 0.7) : EntityAlpha(de\obj, 0.7) : de\ID = 1 : ScaleSprite(de\obj, de\Size, de\Size)
				EntityAlpha(de\obj, Rnd(0.7, 0.85))
			EndIf
		EndIf
		
		If (r\RoomTemplate\Name = "start" And IntroEnabled = False) Or (r\RoomTemplate\Name = "173" And IntroEnabled) Then
			PositionEntity (Collider, EntityX(r\obj), 1.0, EntityZ(r\obj))
			PlayerRoom = r
			
			;CreateNPC(NPCtypeGuard, entityx(r\obj) + 40.0 * RoomScale, 420.0 * RoomScale, entityz(r\obj) + 1052.0 * RoomScale)
		EndIf
		
	Next
	
	Local rt.RoomTemplates
	For rt.RoomTemplates = Each RoomTemplates
		FreeEntity (rt\obj)
	Next	
	
	Local tw.TempWayPoints
	For tw.TempWayPoints = Each TempWayPoints
		Delete tw
	Next
		
	TurnEntity(Collider, 0, Rand(160, 200), 0)
	
	ResetEntity Collider
	
	InitEvents()
	
	MoveMouse 320, 240
	
	SetFont Font1
	
	HidePointer ()
	
	BlinkTimer = BLINKFREQ
	Stamina = 100
	
	For i% = 0 To 70
		FPSfactor = 1.0
		FlushKeys()
		MovePlayer()
		If mpState <> 2 Then
			UpdateDoors()
			UpdateNPCs()
			UpdateWorld()
		EndIf
		;Cls
		DrawLoading(80+Int(Float(i)*0.27))
	Next
	
	DrawLoading(99)
	
	DebugLog "Waiting..."
	
	If mpState = 1 Then
		DebugLog "Server waits"
		Local NumReady = 1
		Repeat
			For i% = 1 To MpNumClients - 1
				If ReadAvail(MpClients(i%)\stream) Then
					Msg$ = ReadLine(MpClients(i%)\stream)
					If Msg$ = "ready" Then
						WriteLine MpClients(i%)\stream,"ok"
						NumReady = NumReady + 1
						DebugLog "Ready. " + NumReady + " out of " + (MpNumClients - 1)
					EndIf
				EndIf
			Next
		Until NumReady = MpNumClients
		For i% = 1 To MpNumClients - 1
			WriteLine MpClients(i%)\stream,"ingame"
		Next
	EndIf
	If mpState = 2 Then
		DebugLog "Client Waits"
		Msg$ = ""
		WriteLine MpStream,"ready"
		Repeat
			Delay 5
			If ReadAvail(MpStream) Then
				Msg$ = ReadLine(MpStream)
			EndIf
		Until Msg$ = "ok"
		Repeat
			Delay 5
			If ReadAvail(MpStream) Then
				Msg$ = ReadLine(MpStream)
			EndIf
		Until Msg$ = "ingame"
	EndIf
	
	DrawLoading(100)
	
	FlushKeys
	FlushMouse
	
	DropSpeed = 0
	
	PrevTime = MilliSecs()
End Function

Function InitLoadGame()
	
	Local d.Doors, sc.SecurityCams, rt.RoomTemplates
	
	DrawLoading(80)
	
	For d.Doors = Each Doors
		EntityParent(d\obj, 0)
		If d\obj2 > 0 Then EntityParent(d\obj2, 0)
		If d\frameobj > 0 Then EntityParent(d\frameobj, 0)
		If d\buttons[0] > 0 Then EntityParent(d\buttons[0], 0)
		If d\buttons[1] > 0 Then EntityParent(d\buttons[1], 0)
		
		;If d\obj2 <> 0 Then
		;	PositionEntity d\obj, EntityX(d\frameobj,True),EntityY(d\frameobj,True),EntityZ(d\frameobj,True)
		;	MoveEntity(d\obj, 0, 0, 8.0 * RoomScale)
		;	PositionEntity d\obj2, EntityX(d\frameobj,True),EntityY(d\frameobj,True),EntityZ(d\frameobj,True)
		;	MoveEntity(d\obj2, 0, 0, 8.0 * RoomScale)
		;EndIf	
	Next
	
	;BurntNote = LoadImage("GFX\items\bn.it")
	;SetBuffer ImageBuffer(BurntNote)
	;Cls
	;Color 0,0,0
	;Text 277, 469, AccessCode, True, True
	;Color 255,255,255
	;SetBuffer BackBuffer()
	
	For sc.SecurityCams = Each SecurityCams
		sc\angle = EntityYaw(sc\obj) + sc\angle
		EntityParent(sc\obj, 0)
	Next
	
	ResetEntity Collider
	
	;InitEvents()
	
	DrawLoading(90)
	
	MoveMouse 320, 240
	
	SetFont Font1
	
	HidePointer ()
	
	BlinkTimer = BLINKFREQ
	Stamina = 100
	
	For rt.RoomTemplates = Each RoomTemplates
		If rt\obj <> 0 Then FreeEntity(rt\obj) : rt\obj = 0
	Next
	
	DropSpeed = 0.01
	
	DrawLoading(100)
	
	PrevTime = MilliSecs()
	FPSfactor = 0	
End Function

Function NullGame()
	Local i%, x%, y%, lvl
	Local itt.ItemTemplates, s.Screens, lt.LightTemplates, d.Doors, m.Materials
	Local wp.WayPoints, twp.TempWayPoints, r.Rooms, it.Items
	
	DoorTempID = 0
	RoomTempID = 0
	
	GameSaved = 0
	
	HideDistance# = 15.0
	
	CameraZoom Camera, 1.0
	
	For lvl = 0 To 0
		For x = 0 To MapWidth - 1
			For y = 0 To MapHeight - 1
				MapTemp(lvl, x, y) = 0
				MapFound(lvl, x, y) = 0
			Next
		Next
	Next
	
	For itt.ItemTemplates = Each ItemTemplates
		itt\found = False
	Next
	
	DropSpeed = 0
	Shake = 0
	CurrSpeed = 0
	
	HeartBeatVolume = 0
	
	Bloodloss = 0
	Injuries = 0
	Infect = 0
	
	For i = 0 To 5
		SCP1025state[i]=0
	Next
	
	SelectedEnding = ""
	EndingTimer = 0
	ExplosionTimer = 0
	
	CameraShake = 0
	Shake = 0
	LightFlash = 0
	
	GodMode = 0
	NoClip = 0
	WearingGasMask = 0
	WearingHazmat = 0
	WearingVest = 0
	Wearing714 = 0
	
	Contained106 = False
	Disabled173 = False
	
	MTFtimer = 0
	For i = 0 To 9
		MTFrooms[i]=Null
		MTFroomState[i]=0
	Next
	
	For s.Screens = Each Screens
		If s\img <> 0 Then FreeImage s\img : s\img = 0
		Delete s
	Next
	
	For i = 0 To MAXACHIEVEMENTS
		Achievements(i)=0
	Next
	RefinedItems = 0
	
	ConsoleInput = ""
	ConsoleOpen = False
	
	PlayerLevel = 0
	
	EyeIrritation = 0
	EyeStuck = 0
	EyeSuper = 0	
	
	ShouldPlay = 0
	
	KillTimer = 0
	FallTimer = 0
	Stamina = 100
	BlurTimer = 0
	SuperMan = False
	SuperManTimer = 0
	
	Msg = ""
	MsgTimer = 0
	
	SelectedItem = Null
	
	For i = 0 To MaxItemAmount - 1
		Inventory(i) = Null
	Next
	SelectedItem = Null
	
	ClosestButton = 0
	
	For d.Doors = Each Doors
		Delete d
	Next
	
	;ClearWorld
	
	For lt.LightTemplates = Each LightTemplates
		Delete lt
	Next 
	
	For  m.Materials = Each Materials
		Delete m
	Next
	
	For wp.WayPoints = Each WayPoints
		Delete wp
	Next
	
	For twp.TempWayPoints = Each TempWayPoints
		Delete twp
	Next	
	
	For r.Rooms = Each Rooms
		Delete r
	Next
	
	For itt.ItemTemplates = Each ItemTemplates
		Delete itt
	Next 
	
	For it.Items = Each Items
		Delete it
	Next
	
	For de.decals = Each Decals
		Delete de
	Next
	
	For n.NPCS = Each NPCs
		Delete n
	Next
	Curr173 = Null
	Curr106 = Null
	Curr096 = Null
	
	Local e.Events
	For e.Events = Each Events
		;If e\Sound<>0 Then FreeSound e\Sound
		Delete e
	Next
	
	For sc.securitycams = Each SecurityCams
		Delete sc
	Next
	
	For em.emitters = Each Emitters
		Delete em
	Next	
	
	For p.particles = Each Particles
		Delete p
	Next		
	DebugLog "d"
	
	For i = 0 To 5
		If ChannelPlaying(RadioCHN(i)) Then StopChannel(RadioCHN(i))
	Next
	
	
	DebugLog "ccccccc"
	
	ClearWorld
	
	
	
End Function

Include "save.bb"

;--------------------------------------- music & sounds ----------------------------------------------

Function PlaySound2%(SoundHandle%, cam%, entity%, range# = 10, volume# = 1.0, play%=True)
	range# = Max(range, 1.0)
	;If Not EntityInView(entity,cam) ChannelVolume soundHandle,0.5
	Local soundchn% = 0
		
		;dis#=Sqr(Abs(EntityX(entity)-EntityX(cam))*Abs(EntityX(entity)-EntityX(cam))+Abs(EntityY(entity)-EntityY(cam))*Abs(EntityY(entity)-EntityY(cam))+Abs(EntityZ(entity)-EntityZ(cam))*Abs(EntityZ(entity)-EntityZ(cam)))
	Local dis# = EntityDistance(cam, entity)
		If 1 - dis# / range# > 0 And 1 - dis# / range# < 1
			Local temp% = CreatePivot()
			PositionEntity temp, EntityX(cam), EntityY(cam), EntityZ(cam)
			PointEntity temp, entity	
			Local panvalue# = Sin(EntityYaw(cam) - EntityYaw(temp))
			
			If play Then soundchn% = PlaySound (SoundHandle)
			
			ChannelVolume(soundchn, volume# * (1 - dis# / range#))
			ChannelPan(soundchn, panvalue)
			;SetChannelRate(soundchn, Rnd(0.95, 1.05))
			
			FreeEntity temp
		EndIf
		
		Return soundchn
End Function

Function LoopSound2%(SoundHandle%, Chn%, cam%, entity%, range# = 10, volume# = 1.0)
	range# = Max(range,1.0)
	;If Not EntityInView(entity,cam) ChannelVolume soundHandle,0.5
		
	;dis#=Sqr(Abs(EntityX(entity)-EntityX(cam))*Abs(EntityX(entity)-EntityX(cam))+Abs(EntityY(entity)-EntityY(cam))*Abs(EntityY(entity)-EntityY(cam))+Abs(EntityZ(entity)-EntityZ(cam))*Abs(EntityZ(entity)-EntityZ(cam)))
	Local dis# = EntityDistance(cam, entity)
	If 1 - dis# / range# > 0 And 1 - dis# / range# < 1 Then
		Local temp% = CreatePivot()
		PositionEntity temp, EntityX(cam), EntityY(cam), EntityZ(cam)
		PointEntity temp, entity	
		Local panvalue# = Sin(EntityYaw(cam) - EntityYaw(temp))
		
		If Chn = 0 Then
			Chn% = PlaySound (SoundHandle)
		Else
			If (Not ChannelPlaying(Chn)) Then Chn% = PlaySound (SoundHandle)
		EndIf
		
		ChannelVolume(Chn, volume# * (1 - dis# / range#))
		ChannelPan(Chn, panvalue)
		;SetChannelRate(soundchn, Rnd(0.95, 1.05))
		
		FreeEntity temp
	EndIf
	
	Return Chn
End Function

Function UpdateMusic()
	
	If NowPlaying <> ShouldPlay Then ; jos soi eri musiikki kuin pit‰isi, vaimennetaan se
		CurrMusicVolume# = Max(CurrMusicVolume - (FPSfactor / 200.0), 0)
		If CurrMusicVolume = 0 Then
			NowPlaying = ShouldPlay
			If MusicCHN <> 0 Then StopChannel MusicCHN
		EndIf
	Else ; jos soi oikea musiikki, volyymi pysyy normaalissa
		CurrMusicVolume = CurveValue(MusicVolume, CurrMusicVolume, 100.0)
	EndIf 		
	
	If ShouldPlay < 66 Then
		If MusicCHN = 0 Then
			MusicCHN = PlaySound(Music(ShouldPlay))
		Else
			If Not ChannelPlaying(MusicCHN) Then MusicCHN = PlaySound(Music(ShouldPlay))
		End If
	EndIf
	
	ChannelVolume MusicCHN, CurrMusicVolume
	
End Function 

Function PauseSounds()
	For e.events = Each Events
		If e\soundchn <> 0 Then
			If ChannelPlaying(e\soundchn) Then PauseChannel(e\soundchn)
		EndIf
		If e\soundchn2 <> 0 Then
			If ChannelPlaying(e\soundchn2) Then PauseChannel(e\soundchn2)
		EndIf		
	Next
	
	For n.npcs = Each NPCs
		If n\soundchn <> 0 Then
			If ChannelPlaying(n\soundchn) Then PauseChannel(n\soundchn)
		EndIf
	Next	
	
	For d.doors = Each Doors
		If d\soundchn <> 0 Then
			If ChannelPlaying(d\soundchn) Then PauseChannel(d\soundchn)
		EndIf
	Next	
	
	If AmbientSFXCHN <> 0 Then
		If ChannelPlaying(AmbientSFXCHN) Then PauseChannel(AmbientSFXCHN)
	EndIf
End Function

Function ResumeSounds()
	For e.events = Each Events
		If e\soundchn <> 0 Then
			If ChannelPlaying(e\soundchn) Then ResumeChannel(e\soundchn)
		EndIf
		If e\soundchn2 <> 0 Then
			If ChannelPlaying(e\soundchn2) Then ResumeChannel(e\soundchn2)
		EndIf	
	Next
	
	For n.npcs = Each NPCs
		If n\soundchn <> 0 Then
			If ChannelPlaying(n\soundchn) Then ResumeChannel(n\soundchn)
		EndIf
	Next	
	
	For d.doors = Each Doors
		If d\soundchn <> 0 Then
			If ChannelPlaying(d\soundchn) Then ResumeChannel(d\soundchn)
		EndIf
	Next	
	
	If AmbientSFXCHN <> 0 Then
		If ChannelPlaying(AmbientSFXCHN) Then ResumeChannel(AmbientSFXCHN)
	EndIf	
End Function

;--------------------------------------- random -------------------------------------------------------

Function f2s$(n#, count%)
	Return Left(n, Len(Int(n))+count+1)
End Function

Function Animate2(ent%, curr#, start%, quit%, speed#, loop=True)
	If speed > 0.0 Then 
		If loop Then 
			SetAnimTime ent, Max(Min(curr + speed * FPSfactor,quit),start)
			If AnimTime(ent) => quit Then SetAnimTime ent, start
			;If AnimTime(ent) < start Then SetAnimTime ent, quit
		Else
			SetAnimTime (ent, Max(Min(curr + speed * FPSfactor,quit),start))
		EndIf
	Else
		If loop Then 
			SetAnimTime (ent, curr + speed * FPSfactor)
			If AnimTime(ent) < quit Then SetAnimTime ent, start
			If AnimTime(ent) > start Then SetAnimTime ent, quit
		Else
			SetAnimTime (ent, Max(Min(curr + speed * FPSfactor,start),quit))
		EndIf
	EndIf
	
End Function 

Function Use914(item.Items, setting$, x#, y#, z#)
	
	RefinedItems = RefinedItems+1
	
	Local it2.Items, texture%
	Select item\itemtemplate\name
		Case "Document SCP-106", "Note", "Notification",  "Document", "Security Clearance Levels", "Mobile Task Forces", "Object Classes", "Document SCP-173", "Document SCP-895", "Document SCP-079", "Origami", "Document SCP-860", "Document SCP-682", "Document SCP-860-1", "SCP-093 Recovered Materials"
			Select setting
				Case "rough", "coarse"
					Local d.Decals = CreateDecal(7, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.12 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					Select Rand(6)
						Case 1
							it2 = CreateItem("Document SCP-106", "paper", x, y, z)
						Case 2
							it2 = CreateItem("Document SCP-079", "paper", x, y, z)
						Case 3
							it2 = CreateItem("Document SCP-173", "paper", x, y, z)
						Case 4
							it2 = CreateItem("Document SCP-895", "paper", x, y, z)
						Case 5
							it2 = CreateItem("Document SCP-682", "paper", x, y, z)
						Case 6
							it2 = CreateItem("Document SCP-860", "paper", x, y, z)
					End Select
				Case "fine", "very fine"
					it2 = CreateItem("Origami", "misc", x, y, z)
			End Select
			
			RemoveItem(item)
		Case "Gas Mask"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(7, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.12 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					PositionEntity(item\obj, x, y, z)
					ResetEntity(item\obj)
					RemoveItem(item)
				Case "fine", "very fine"
					it2 = CreateItem("Gas Mask", "supergasmask", x, y, z)
					RemoveItem(item)
			End Select
		Case "Ballistic Best"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(7, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.12 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					PositionEntity(item\obj, x, y, z)
					ResetEntity(item\obj)
					RemoveItem(item)
				Case "fine"
					it2 = CreateItem("Heavy Ballistic Vest", "finevest", x, y, z)
					RemoveItem(item)
				Case "very fine"
					it2 = CreateItem("Bulky Ballistic Vest", "finevest", x, y, z)
					RemoveItem(item)
			End Select
		Case "First Aid Kit"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(7, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.12 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateItem("Blue First Aid Kit", "firstaid2", x, y, z)
					RemoveItem(item)
				Case "fine"
					it2 = CreateItem("Small First Aid Kit", "finefirstaid", x, y, z)
					RemoveItem(item)
				Case "very fine"
					it2 = CreateItem("Strange Bottle", "veryfinefirstaid", x, y, z)
					RemoveItem(item)
			End Select
		Case "Level 1 Key Card", "Level 2 Key Card", "Level 3 Key Card", "Level 4 Key Card", "Level 5 Key Card", "Key Card"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(7, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.07 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateItem("Playing Card", "misc", x, y, z)
				Case "fine"
					If Rand(6)=1 Then 
						it2 = CreateItem("Playing Card", "misc", x, y, z)
					Else
						Select item\itemtemplate\name
							Case "Level 1 Key Card"
								it2 = CreateItem("Level 2 Key Card", "key2", x, y, z)
							Case "Level 2 Key Card"
								it2 = CreateItem("Level 3 Key Card", "key3", x, y, z)
							Case "Level 3 Key Card"
								it2 = CreateItem("Level 4 Key Card", "key4", x, y, z)
							Case "Level 4 Key Card"
								it2 = CreateItem("Level 5 Key Card", "key5", x, y, z)
							Case "Level 5 Key Card"	
								it2 = CreateItem("Key Card Omni", "key6", x, y, z)
						End Select						
					EndIf
				Case "very fine"
					If Rand(3)=1 Then
						it2 = CreateItem("Mastercard", "misc", x, y, z)
					Else	
						it2 = CreateItem("Key Card Omni", "key6", x, y, z)	
					EndIf
					
			End Select			
			
			RemoveItem(item)
		Case "Key Card Omni"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(7, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.07 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					If Rand(2)=1 Then
						it2 = CreateItem("Mastercard", "misc", x, y, z)
					Else
						it2 = CreateItem("Playing Card", "misc", x, y, z)			
					EndIf	
				Case "fine", "very fine"
					it2 = CreateItem("Key Card Omni", "key6", x, y, z)
			End Select			
			
			RemoveItem(item)
		Case "Playing Card"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(7, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.07 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1", "fine", "very fine"
					If Rand(2)=1 Then
						it2 = CreateItem("Mastercard", "misc", x, y, z)				
					Else
						it2 = CreateItem("Level 2 Key Card", "key2", x, y, z)	
					EndIf
			End Select
			RemoveItem(item)
		Case "S-NAV 300 Navigator", "S-NAV 310 Navigator", "S-NAV Navigator", "S-NAV Navigator Ultimate"
			Select setting
				Case "rough", "coarse"
					it2 = CreateItem("Electronical components", "misc", x, y, z)
				Case "1:1"
					it2 = CreateItem("S-NAV Navigator", "nav", x, y, z)
					it2\state = 100
				Case "fine"
					it2 = CreateItem("S-NAV 310 Navigator", "nav", x, y, z)
					it2\state = 100
				Case "very fine"
					it2 = CreateItem("S-NAV Navigator Ultimate", "nav", x, y, z)
					it2\state = 101
			End Select
			
			RemoveItem(item)
		Case "Radio Transceiver"
			Select setting
				Case "rough", "coarse"
					it2 = CreateItem("Electronical components", "misc", x, y, z)
				Case "1:1"
					it2 = CreateItem("Radio Transceiver", "18vradio", x, y, z)
					it2\state = 100
				Case "fine"
					it2 = CreateItem("Radio Transceiver", "fineradio", x, y, z)
					it2\state = 101
				Case "very fine"
					it2 = CreateItem("Radio Transceiver", "veryfineradio", x, y, z)
					it2\state = 101
			End Select
			
			RemoveItem(item)			
		Case "Some SCP-420-J", "Cigarette"
			Select setting
				Case "rough", "coarse"			
					d.Decals = CreateDecal(0, x, 8*RoomScale+0.010, z, 90, Rand(360), 0)
					d\Size = 0.2 : EntityAlpha(d\obj, 0.8) : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateItem("Cigarette", "cigarette", x + 1.5, y + 0.5, z + 1.0)
				Case "fine"
					it2 = CreateItem("Joint", "420s", x + 1.5, y + 0.5, z + 1.0)
				Case "very fine"
					it2 = CreateItem("Smelly Joint", "420s", x + 1.5, y + 0.5, z + 1.0)
			End Select
			
			RemoveItem(item)
		Case "9V Battery", "18V Battery", "Strange Battery"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.010, z, 90, Rand(360), 0)
					d\Size = 0.2 : EntityAlpha(d\obj, 0.8) : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateItem("18V Battery", "18vbat", x, y, z)
				Case "fine"
					it2 = CreateItem("Strange Battery", "killbat", x, y, z)
				Case "very fine"
					it2 = CreateItem("Strange Battery", "killbat", x, y, z)
			End Select
			
			RemoveItem(item)
		Case "ReVision Eyedrops", "RedVision Eyedrops", "Eyedrops"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.010, z, 90, Rand(360), 0)
					d\Size = 0.2 : EntityAlpha(d\obj, 0.8) : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateItem("RedVision Eyedrops", "eyedrops", x,y,z)
				Case "fine"
					it2 = CreateItem("Eyedrops", "fineeyedrops", x,y,z)
				Case "very fine"
					it2 = CreateItem("Eyedrops", "supereyedrops", x,y,z)
			End Select
			
			RemoveItem(item)			
		Default
			PositionEntity(item\obj, x, y, z)
			ResetEntity(item\obj)
	End Select
	
	If it2 <> Null Then
		EntityType (it2\obj, HIT_ITEM)
	End If
End Function

Function UpdateMTF%()
	If PlayerRoom\RoomTemplate\Name = "exit1" Then Return
	
	Local r.Rooms, n.NPCs, Exit1.Rooms
	If MTFSFX(0) = 0 Then
		
		For r.Rooms = Each Rooms
			If Lower(r\RoomTemplate\Name) = "exit1" Then Exit1 = r : Exit
		Next
		
		If Exit1 <> Null Then 
			If Abs(EntityZ(Exit1\obj)-EntityZ(Collider))<25.0 Then
				For r.Rooms = Each Rooms
					Select Lower(r\RoomTemplate\Name)
						Case "exit1"
							Exit1 = r
						Case "room106"
							MTFrooms[0]=r
						Case "roompj"
							MTFrooms[1]=r	
						Case "room079"
							MTFrooms[2]=r	
						Case "room2poffices"
							MTFrooms[3]=r	
						Case "914"
							MTFrooms[4]=r	
						Case "coffin"
							MTFrooms[5]=r	
						Case "start"
							MTFrooms[6]=r		
					End Select
				Next
				
				MTFSFX(0)=LoadSound("SFX\MTF\Stop0.ogg")
				MTFSFX(1)=LoadSound("SFX\MTF\Stop1.ogg")
				MTFSFX(2)=LoadSound("SFX\MTF\Stop2.ogg")			
				MTFSFX(3)=LoadSound("SFX\MTF\ClassD0.ogg")
				MTFSFX(4)=LoadSound("SFX\MTF\ClassD1.ogg")
				MTFSFX(5)=LoadSound("SFX\MTF\Beep.ogg")					
			EndIf
		EndIf
		
	EndIf	
	
	;mtf ei viel‰ spawnannut, spawnataan jos pelaaja menee tarpeeksi l‰helle gate b:t‰
	If MTFtimer = 0 Then
		If Rand(30)=1 Then
			
			If Exit1 = Null Then
				For r.Rooms = Each Rooms
					If Lower(r\RoomTemplate\Name) = "exit1" Then Exit1 = r : Exit
				Next
			EndIf
			
			If Exit1 <> Null Then 
				If Abs(EntityZ(Exit1\obj)-EntityZ(Collider))<30.0 Then
					TempSound=LoadSound("SFX\MTF\Announc.ogg")
					PlaySound TempSound
					
					;Stop
					MTFtimer = 1
					For i = 0 To 2
						n.NPCs = CreateNPC(NPCtypeMTF, EntityX(Exit1\obj)+0.3*i+Sin(Exit1\angle)*5.0, 0.5,EntityZ(Exit1\obj)+0.3*i-Cos(Exit1\angle)*5.0)
						n\PrevState = 0
						n\PrevX = i
						If i > 0 Then
							n\Target = Before n
							n\State = 4
						EndIf
					Next
				EndIf
			EndIf
			
		EndIf
		
		
	Else
		;mtf spawnannut, aletaan p‰ivitt‰‰ teko‰ly‰
		
		MTFtimer=MTFtimer+FPSfactor
		
		;mtfroomstate 0 = huonetta ei ole alettu viel‰ etsi‰
		;mtfroomstate 1 = joku tiimi on menossa huoneeseen
		;mtfroomstate 2 = huone on tarkistettu
		;mtfroomstate 3 = huoneeseen ei lˆydetty reitti‰ -> yritet‰‰n v‰h‰n ajan p‰‰st‰ uudestaan
		
		;prevstate 0 = k‰y l‰pi tutkimattomia huoneita
		
		;p‰ivitet‰‰n kymmenen sekunnin v‰lein MTF:n "kollektiivinen teko‰ly"
		If MTFtimer > (70*10) Then
			
			;tiimi saapunut 106:n huoneeseen, "pyydystet‰‰n" se
			If MTFrooms[0]<>Null Then
				If MTFroomState[0]=2 Then
					If PlayerRoom\RoomTemplate\Name<>"room106" Then
						If Contained106 Then
							If TempSound<>0 Then FreeSound(TempSound) : TempSound = 0
							TempSound = LoadSound("SFX\MTF\Oldman2.ogg")
							PlayMTFMessage(TempSound)
							MTFroomState[0]=4
						ElseIf Curr106\State>0 
							If TempSound<>0 Then FreeSound(TempSound) : TempSound = 0
							TempSound = LoadSound("SFX\MTF\Oldman1.ogg")
							PlayMTFMessage(TempSound)
							Contained106=True
							MTFroomState[0]=4
						EndIf
					EndIf
				EndIf
			EndIf
			
			For i = 0 To 6
				If MTFroomState[i]=1 Then MTFroomState[i] = 0
				
				If MTFroomState[i]=3 Then
					DebugLog "ei reitti‰ ("+MTFrooms[i]\RoomTemplate\Name+"), ohitetaan"
					If Rand(8)=1 Then MTFroomState[i] = 0
				EndIf		
			Next
			
			For n.NPCs = Each NPCs
				If n\NPCtype = NPCtypeMTF And n\PrevState = 0 And n\State2 <= 0 And n\Target = Null Then
					DebugLog "asdfadsfasdfsdafasdfasdf"
					If n\PathStatus <> 1 Then 
							
						;etsit‰‰n reitti l‰himp‰‰n huoneeseen jota ei ole viet‰ k‰yty tutkimassa
						Local closestroom%, closestroomdist#=500.0
						For i = 0 To 6
							If MTFrooms[i]<>Null Then
								If MTFroomState[i] = 0 Then 
									dist# = EntityDistance(n\Collider, MTFrooms[i]\obj)
									If dist < closestroomdist Then
										closestroomdist = dist
										closestroom = i
									EndIf
								EndIf
							EndIf
						Next
						
						If closestroomdist < 500.0 Then
							DebugLog MTFrooms[closestroom]\RoomTemplate\Name+": "+closestroomdist
							
							If Distance(EntityX(MTFrooms[closestroom]\obj,True),EntityZ(MTFrooms[closestroom]\obj,True),EntityX(n\Collider),EntityZ(n\Collider))< 4.0 Then
								;tiimi saapunut huoneeseen, merkataan ett‰ se on tarkistettu
								MTFroomState[closestroom]=2
								
								Select MTFrooms[closestroom]\RoomTemplate\Name 
									Case "room106"
										TempSound = LoadSound("SFX\MTF\Oldman0.ogg")
										n\SoundChn = PlaySound2(TempSound, Camera, n\Collider, 8.0)
										PlayMTFMessage(TempSound)
										
										n\PathStatus = FindPath(n, EntityX(MTFrooms[closestroom]\Objects[9],True),EntityY(MTFrooms[closestroom]\Objects[9],True),EntityZ(MTFrooms[closestroom]\Objects[9],True))
										n\PathTimer = 70*30
										n\State=3										
									Default
										For n2.npcs = Each NPCs
											If n2 <> n And n2\PrevState = n\PrevState And n2\NPCtype = NPCtypeMTF Then
												n2\state = 0
											EndIf
										Next															
								End Select
								
								DebugLog "room found"
							Else
								n\PathStatus = FindPath(n, EntityX(MTFrooms[closestroom]\obj), 0.2, EntityZ(MTFrooms[closestroom]\obj))
								
								If n\PathStatus = 2 Then 
									;merkataan ettei huoneeseen lˆytynyt reitti‰, yritet‰‰n uudestaan jonkin ajan p‰‰st‰
									MTFroomState[closestroom]=3
									DebugLog "reitti‰ ei lˆytynyt, roomstate "+MTFroomState[closestroom]
									
								ElseIf n\PathStatus = 1
									;merkataan ett‰ tiimi on menossa huoneeseen,
									;jottei mik‰‰n muu tiimi l‰hde menem‰‰n samaan huoneeseen					
									
									;For n2.npcs = Each NPCs
									;	If n2 <> n And n2\PrevState = n\PrevState And n2\NPCtype = NPCtypeMTF Then
									;		n2\state = 4
									;		n2\target = n
									;	EndIf
									;Next
									
									MTFroomState[closestroom]=1 
									n\State = 3
								EndIf								
							EndIf
							
						EndIf
						
					EndIf
					
					Exit
				EndIf
			Next
			
			MTFtimer = 1.0
		EndIf
		
	EndIf
End Function


Function UpdateInfect()
	Local temp#, i%, r.Rooms, n.NPCs
	
	If Infect>0 Then
		ShowEntity InfectOverlay
		
		If Infect < 93.0 Then
			temp=Infect
			Infect = Min(Infect+FPSfactor*0.002,100)
			
			BlurTimer = Max(Infect*3*(2.0-CrouchState),BlurTimer)
			
			HeartBeatRate = Max(HeartBeatRate, 100)
			HeartBeatVolume = Max(HeartBeatVolume, Infect/120.0)
			
			EntityAlpha InfectOverlay, Min(((Infect*0.2)^2)/1000.0,0.5) * (Sin(MilliSecs()/8.0)+2.0)
			
			For i = 0 To 5
				If Infect>i*15+10 And temp =< i*15+10 Then
					If TempSound<>0 Then FreeSound TempSound
					TempSound = LoadSound("SFX\008voices"+i+".ogg")
					PlaySound TempSound
				EndIf
			Next
			
			If Infect > 40 And temp =< 40.0 Then
				Msg = "You feel like feverish."
				MsgTimer = 70*6
			ElseIf Infect > 80 And temp =< 80.0
				Msg = "You feel very faint..."
				MsgTimer = 70*4
			ElseIf Infect =>91.5
				BlinkTimer = Max(Min(-10*(Infect-91.5),BlinkTimer),-10)
				If Infect >= 92.7 And temp < 92.7 Then
					For r.Rooms = Each Rooms
						If r\RoomTemplate\Name="008" Then
							PositionEntity Collider, EntityX(r\Objects[3],True),EntityY(r\Objects[3],True),EntityZ(r\Objects[3],True),True
							ResetEntity Collider
							r\NPC[0] = CreateNPC(NPCtypeD, EntityX(r\Objects[2],True),EntityY(r\Objects[2],True)+0.2,EntityZ(r\Objects[2],True))
							r\NPC[0]\Sound = LoadSound("SFX\008death1.ogg")
							r\NPC[0]\SoundChn = PlaySound(r\NPC[0]\Sound)
							tex = LoadTexture("GFX\npcs\scientist2.jpg")
							EntityTexture r\NPC[0]\obj, tex
							r\NPC[0]\State=2
							PlayerRoom = r
							Exit
						EndIf
					Next
				EndIf
			EndIf
		Else
			temp=Infect
			Infect = Min(Infect+FPSfactor*0.004,100)
			
			If Infect < 94.7 Then
				EntityAlpha InfectOverlay, 0.5 * (Sin(MilliSecs()/8.0)+2.0)
				BlurTimer = 900
				
				If Infect > 94.5 Then BlinkTimer = Max(Min(-50*(Infect-94.5),BlinkTimer),-10)
				PointEntity Collider, PlayerRoom\NPC[0]\Collider
				PointEntity PlayerRoom\NPC[0]\Collider, Collider
				ForceMove = 0.4
				Injuries = 2.5
				Bloodloss = 0
				
				Animate2(PlayerRoom\NPC[0]\obj, AnimTime(PlayerRoom\NPC[0]\obj), 357, 381, 0.3)
			ElseIf Infect < 98.5
				
				EntityAlpha InfectOverlay, 0.5 * (Sin(MilliSecs()/5.0)+2.0)
				BlurTimer = 950
				
				If temp < 94.7 Then 
					PlayerRoom\NPC[0]\Sound = LoadSound("SFX\008death2.ogg")
					PlayerRoom\NPC[0]\SoundChn = PlaySound(PlayerRoom\NPC[0]\Sound)
					
					Kill()
					de.Decals = CreateDecal(3, EntityX(PlayerRoom\NPC[0]\Collider), 544*RoomScale + 0.01, EntityZ(PlayerRoom\NPC[0]\Collider),90,Rnd(360),0)
					de\Size = 0.8
					ScaleSprite(de\obj, de\Size,de\Size)
				ElseIf Infect > 96
					BlinkTimer = Max(Min(-10*(Infect-96),BlinkTimer),-10)
				Else
					KillTimer = Max(-350, KillTimer)
				EndIf
				
				If PlayerRoom\NPC[0]\State2=0 Then
					Animate2(PlayerRoom\NPC[0]\obj, AnimTime(PlayerRoom\NPC[0]\obj), 13, 19, 0.3,False)
					If AnimTime(PlayerRoom\NPC[0]\obj) => 19 Then PlayerRoom\NPC[0]\State2=1
				Else
					Animate2(PlayerRoom\NPC[0]\obj, AnimTime(PlayerRoom\NPC[0]\obj), 19, 13, -0.3)
					If AnimTime(PlayerRoom\NPC[0]\obj) =< 13 Then PlayerRoom\NPC[0]\State2=0
				EndIf
				
				If Rand(50)=1 Then
					p.Particles = CreateParticle(EntityX(PlayerRoom\NPC[0]\Collider),EntityY(PlayerRoom\NPC[0]\Collider),EntityZ(PlayerRoom\NPC[0]\Collider), 5, Rnd(0.05,0.1), 0.15, 200)
					p\speed = 0.01
					p\SizeChange = 0.01
					p\A = 0.5
					p\Achange = -0.01
					RotateEntity p\pvt, Rnd(360),Rnd(360),0
				EndIf
				
				PositionEntity Head, EntityX(PlayerRoom\NPC[0]\Collider,True), EntityY(PlayerRoom\NPC[0]\Collider,True)+0.65,EntityZ(PlayerRoom\NPC[0]\Collider,True),True
				RotateEntity Head, (1.0+Sin(MilliSecs()/5.0))*15, PlayerRoom\angle-180, 0, True
				MoveEntity Head, 0,0,0.4
				TurnEntity Head, 80+(Sin(MilliSecs()/5.0))*30,(Sin(MilliSecs()/5.0))*40,0
			EndIf
		EndIf
		
		
	Else
		HideEntity InfectOverlay
	EndIf
End Function


Type SecurityCams
	Field obj%, MonitorObj%
	
	Field BaseObj%, CameraObj%
	
	Field ScrObj%, ScrWidth#, ScrHeight#
	Field Screen%, Cam%, ScrTexture%, ScrOverlay%
	Field angle#, turn#, CurrAngle#
	Field State#, PlayerState%
	
	Field soundCHN%
	
	Field InSight%
	
	Field room.Rooms
	
	Field FollowPlayer%
	Field Cursed%;, CurseTimer#
	
	Field MinAngle#, MaxAngle#, dir%
End Type

Function CreateSecurityCam.SecurityCams(x#, y#, z#, r.Rooms, screen% = False)
	Local sc.SecurityCams = New SecurityCams
	
	sc\obj = CopyMesh(CamBaseOBJ)
	ScaleEntity(sc\obj, 0.0015, 0.0015, 0.0015)
	sc\CameraObj = CopyMesh(CamOBJ)
	ScaleEntity(sc\CameraObj, 0.01, 0.01, 0.01)
	
	sc\room = r
	
	sc\Screen = screen
	If screen Then
		Local scale# = RoomScale * 4.5 * 0.4
		
		sc\ScrObj = CreateSprite()
		EntityFX sc\ScrObj, 17
		SpriteViewMode(sc\ScrObj, 2)
		sc\ScrTexture = CreateTexture(512, 512)
		EntityTexture sc\ScrObj, sc\ScrTexture
		ScaleSprite(sc\ScrObj, MeshWidth(Monitor) * scale * 0.95* 0.5, MeshHeight(Monitor) * scale * 0.95* 0.5)
		
		sc\ScrOverlay = CreateSprite(sc\ScrObj)
		;	scaleSprite(sc\scrOverlay , 0.5, 0.4)
		ScaleSprite(sc\ScrOverlay, MeshWidth(Monitor) * scale * 0.95 * 0.5, MeshHeight(Monitor) * scale * 0.95 * 0.5)
		MoveEntity(sc\ScrOverlay, 0, 0, -0.005)
		EntityTexture(sc\ScrOverlay, MonitorTexture)
		SpriteViewMode(sc\ScrOverlay, 2)
		EntityBlend(sc\ScrOverlay , 3)
		
		sc\MonitorObj = CopyMesh(Monitor, sc\ScrObj)
		
		ScaleEntity(sc\MonitorObj, scale, scale, scale)
		
		sc\Cam = CreateCamera()
		;PositionEntity (sc\cam, x, y, z)
		;cameraClsMode Cam, 0, 0
		;cameraFogMode (sc\cam, 1)
		;cameraFogRange (sc\cam, 0.5, 5)
		;cameraFogColor (sc\cam, 0, 0, 0)
		CameraViewport(sc\Cam, 0, 0, 512, 512)
		CameraRange sc\Cam, 0.05, 6.0
		CameraZoom(sc\Cam, 0.8)
		HideEntity(sc\Cam)	
	End If
	
	PositionEntity(sc\obj, x, y, z)
	
	Return sc
End Function

Function UpdateSecurityCams()
	MoveLock = False
	For sc.SecurityCams = Each SecurityCams
		
		close = False
		
		If Abs(EntityX(Collider)-EntityX(sc\obj,True))<4.0 Then 
			If Abs(EntityZ(Collider)-EntityZ(sc\obj,True))<4.0 Then	
				If EntityDistance(Camera, sc\obj) < 4.0 Then close = True : DebugLog sc\room\roomtemplate\name
			EndIf
		EndIf
		
		If close = False Then
			If sc\scrobj <> 0 Then
				If Abs(EntityX(Collider)-EntityX(sc\scrobj,True))<3.0 Then 
					If Abs(EntityZ(Collider)-EntityZ(sc\scrobj,True))<3.0 Then	
						If EntityDistance(Camera, sc\scrobj) < 3.0 Then close = True
					EndIf
				EndIf				
			EndIf
		EndIf
		
		If close = True Then 
			
			If sc\FollowPlayer Then
				PointEntity(sc\cameraObj, Camera)
				Local temp# = EntityPitch(sc\cameraobj)
				RotateEntity(sc\obj, 0, CurveAngle(EntityYaw(sc\cameraObj), EntityYaw(sc\obj), 75.0), 0)
				
				If temp < 40.0 Then temp = 40
				If temp > 70.0 Then temp = 70
				RotateEntity(sc\cameraObj, CurveAngle(temp, EntityPitch(sc\cameraObj), 75.0), EntityYaw(sc\obj), 0)
				
				
				PositionEntity(sc\cameraObj, EntityX(sc\obj, True), EntityY(sc\obj, True) - 0.083, EntityZ(sc\obj, True))
				RotateEntity(sc\cameraObj, EntityPitch(sc\cameraObj), EntityYaw(sc\obj), 0)
			Else
				If sc\turn > 0 Then
					If sc\dir = 0 Then
						sc\currangle=sc\currangle+0.2 * FPSfactor
						If sc\currangle > (sc\turn * 1.3) Then sc\dir = 1
					Else
						sc\currangle=sc\currangle-0.2 * FPSfactor
						If sc\currangle < (-sc\turn * 1.3) Then sc\dir = 0
					End If
				End If
				RotateEntity(sc\obj, 0, sc\room\angle + sc\angle + Max(Min(sc\currangle, sc\turn), -sc\turn), 0)
				
				PositionEntity(sc\cameraObj, EntityX(sc\obj, True), EntityY(sc\obj, True) - 0.083, EntityZ(sc\obj, True))
				RotateEntity(sc\cameraObj, EntityPitch(sc\cameraObj), EntityYaw(sc\obj), 0)
				
				If sc\cam<>0 Then 
					PositionEntity(sc\cam, EntityX(sc\cameraObj, True), EntityY(sc\cameraObj, True), EntityZ(sc\cameraObj, True))
					RotateEntity(sc\cam, EntityPitch(sc\cameraObj), EntityYaw(sc\cameraObj), 0)
					MoveEntity(sc\cam, 0, 0, 0.1)
				EndIf 
			EndIf
			
			sc\state = sc\state+FPSfactor
			If sc\screen And sc\state >= 12 Then
				sc\inSight = False
				If EntityInView(sc\scrObj, Camera) Then
					If EntityVisible(Camera,sc\scrObj) Then
						If BlinkTimer > - 5 Then 
							sc\inSight = True
							
							If sc\cursed Then
								If BlinkTimer > - 5 Then Sanity=Sanity-(FPSfactor * 16)
								
								If Sanity < (-1000) Then Kill()				
							End If
							
							HideEntity(Camera)
							ShowEntity(sc\cam)
							Cls
							RenderWorld
							CopyRect (0, 0, 512, 512, 0, 0, 0, TextureBuffer(sc\scrTexture))
							HideEntity(sc\cam)
							ShowEntity(Camera)
						EndIf
					EndIf
				EndIf
				sc\state = 0
			End If
			
			If sc\cursed Then
				If sc\inSight Then
					Local pvt% = CreatePivot()
					PositionEntity pvt, EntityX(Camera), EntityY(Camera), EntityZ(Camera)
					PointEntity(pvt, sc\scrObj)
					TurnEntity(pvt, 90, 0, 0)
					user_camera_pitch = CurveAngle(EntityPitch(pvt), user_camera_pitch + 90.0, Min(Max(15000.0 / (-Sanity), 15.0), 200.0))
					user_camera_pitch=user_camera_pitch-90
					RotateEntity(Collider, EntityPitch(Collider), CurveAngle(EntityYaw(pvt), EntityYaw(Collider), Min(Max(15000.0 / (-Sanity), 15.0), 200.0)), 0)
					FreeEntity pvt
					
					If Sanity < - 800 Then
						If Rand(3) = 1 Then EntityTexture(sc\scrOverlay, MonitorTexture)
						If Rand(6) < 5 Then
							EntityTexture(sc\scrOverlay, GorePics(Rand(0, 5)))
							If sc\PlayerState = 1 Then PlaySound(HorrorSFX(1))
							sc\playerState = 2
							If sc\soundCHN = 0 Then
								sc\soundCHN = PlaySound(HorrorSFX(4))
							Else
								If Not ChannelPlaying(sc\soundCHN) Then sc\soundCHN = PlaySound(HorrorSFX(4))
							End If
						End If	
						BlurTimer = 1000
					ElseIf Sanity < - 500
						If Rand(7) = 1 Then EntityTexture(sc\scrOverlay, MonitorTexture)
						If Rand(50) = 1 Then
							EntityTexture(sc\scrOverlay, GorePics(Rand(0, 5)))
							If sc\playerState = 0 Then PlaySound(HorrorSFX(0))
							sc\playerState = Max(sc\playerState, 1)
						End If
					Else
						EntityTexture(sc\scrOverlay, MonitorTexture)
					EndIf
				EndIf
				
			ElseIf sc\inSight
				If sc\playerState = 0 Then
					sc\playerState = Rand(60000, 65000)
				EndIf
				If Rand(500) = 1 Then
					EntityTexture(sc\scrOverlay, OldAiPics(0))
				End If
				
				If (MilliSecs() Mod sc\playerState) >= Rand(500) Then
					EntityTexture(sc\scrOverlay, MonitorTexture)
				Else
					EntityTexture(sc\scrOverlay, OldAiPics(0))
				EndIf
				
			EndIf
			
		EndIf
	Next
	
	Cls
	
	
End Function


Function UpdateLever(obj, locked=False)
	
	Local dist# = EntityDistance(Camera, obj)
	If dist < 8.0 Then 
		If dist < 0.8 And (Not locked) Then 
			If EntityInView(obj, Camera) Then 
				
				EntityPick(Camera, 0.65)
				
				If PickedEntity() = obj Then
					DrawHandIcon = True
					If MouseHit1 Then GrabbedEntity = obj
				End If
				
				prevpitch# = EntityPitch(obj)
				
				If (MouseDown1 Or MouseHit1) Then
					If GrabbedEntity <> 0 Then
						If GrabbedEntity = obj Then
							DrawHandIcon = True 
							;TurnEntity(obj, , 0, 0)
							RotateEntity(GrabbedEntity, Max(Min(EntityPitch(obj)+Min(mouse_y_speed_1 * 4,5.0), 82), -82), EntityYaw(obj), 0)
							
							DrawArrowIcon(0) = True
							DrawArrowIcon(2) = True
							
						EndIf
					EndIf
				EndIf 
				
				If EntityPitch(obj,True) > 80 Then ;p‰‰ll‰
					If prevpitch =< 80 Then PlaySound2(LeverSFX, Camera, obj, 1.0)
				ElseIf EntityPitch(obj,True) < -80 ;pois p‰‰lt‰
					If prevpitch => -80 Then PlaySound2(LeverSFX, Camera, obj, 1.0)	
				EndIf						
			EndIf
		EndIf
		
		If MouseDown1=False And MouseHit1=False Then 
			If EntityPitch(obj,True) > 0 Then
				RotateEntity(obj, CurveValue(82, EntityPitch(obj), 10), EntityYaw(obj), 0)
			Else
				RotateEntity(obj, CurveValue(-82, EntityPitch(obj), 10), EntityYaw(obj), 0)
			EndIf
			GrabbedEntity = 0
		End If
		
		If EntityPitch(obj,True) > 0 Then ;p‰‰ll‰
			Return True
		Else ;pois p‰‰lt‰
			Return False
		EndIf	
		
	EndIf
	
End Function

Function UpdateButton(obj)
	
	Local dist# = EntityDistance(Collider, obj);entityDistance(collider, d\buttons[i])
	If dist < 0.8 Then
		Local temp% = CreatePivot()
		PositionEntity temp, EntityX(Camera), EntityY(Camera), EntityZ(Camera)
		PointEntity temp,obj
		
		If EntityPick(temp, 0.65) = obj Then
			If ClosestButton = 0 Then 
				ClosestButton = obj
			Else
				If dist < EntityDistance(Collider, ClosestButton) Then ClosestButton = obj
			End If							
		End If
		
		FreeEntity temp
	EndIf			
	
End Function

Function UpdateElevators#(State#, door1.Doors, door2.Doors, room1, room2, event.Events)
	If door1\open = True And door2\open = False Then 
		State = -1
		If (ClosestButton = door2\buttons[0] Or ClosestButton = door2\buttons[1]) And MouseHit1 Then
			UseDoor(door1,True)
		EndIf
	ElseIf door2\open = True And door1\open = False
		State = 1
		If (ClosestButton = door1\buttons[0] Or ClosestButton = door1\buttons[1]) And MouseHit1 Then
			UseDoor(door2,True)
		EndIf					
	EndIf
	
	DebugLog State+ " - "+door1\open +", "+door2\open
	
	Local inside = False
	
	;molemmat ovet kiinni = hissi liikkuu
	If door1\open = False And door2\open = False Then
		DebugLog "bim"
		door1\locked = True 
		door2\locked = True 
		If State < 0 Then ;ylh‰‰lt‰ alas
			DebugLog "ylh‰‰lt‰ alas - "+Abs(EntityX(Collider)-EntityX(room1,True))+", "+Abs(EntityZ(Collider)-EntityZ(room1,True))
			State = State - FPSfactor
			;pelaaja hissin sis‰ll‰
			If Abs(EntityX(Collider)-EntityX(room1,True))<280.0*RoomScale Then
				If Abs(EntityZ(Collider)-EntityZ(room1,True))<280.0*RoomScale Then	
					inside = True
					
					If event\SoundCHN = 0 Then
						event\SoundCHN = PlaySound(ElevatorMoveSFX)
					Else
						If (Not ChannelPlaying(event\SoundCHN)) Then event\SoundCHN = PlaySound(ElevatorMoveSFX)
					EndIf
					
					CameraShake = Sin(State/3.0)*0.3
				EndIf
			EndIf
			
			If State < -400 Then
				door1\locked = False
				door2\locked = False				
				State = 0
				
				UseDoor(door2,True)							
				
				If inside Then
					PositionEntity(Collider, EntityX(room2,True)+(EntityX(Collider)-EntityX(room1,True)),0.05+EntityY(room2,True)+(EntityY(Collider)-EntityY(room1,True)),EntityZ(room2,True)+(EntityZ(Collider)-EntityZ(room1,True)),True)
					ResetEntity Collider	
					UpdateDoors()
					UpdateRooms()
				EndIf
				
				PlaySound2(ElevatorBeepSFX, Camera, room1, 4.0)	
				;PlaySound(ElevatorBeepSFX)	
			EndIf
		Else ;alhaalta ylˆs
			DebugLog "alhaalta ylˆs - " +Abs(EntityX(Collider)-EntityX(room2,True))+", "+Abs(EntityZ(Collider)-EntityZ(room2,True))
			State = State + FPSfactor
			;pelaaja hissin sis‰ll‰
			If Abs(EntityX(Collider)-EntityX(room2,True))<280.0*RoomScale Then
				If Abs(EntityZ(Collider)-EntityZ(room2,True))<280.0*RoomScale Then	
					inside = True
					
					If event\SoundCHN = 0 Then
						event\SoundCHN = PlaySound(ElevatorMoveSFX)
					Else
						If (Not ChannelPlaying(event\SoundCHN)) Then event\SoundCHN = PlaySound(ElevatorMoveSFX)
					EndIf
					
					CameraShake = Sin(State/3.0)*0.3
				EndIf
			EndIf	
			
			If State > 400 Then 
				door1\locked = False
				door2\locked = False				
				State = 0
				
				UseDoor(door1,True)	
				
				;pelaaja hissin sis‰ll‰, siirret‰‰n
				If inside Then	
					PositionEntity(Collider, EntityX(room1,True)+(EntityX(Collider)-EntityX(room2,True)),0.05+EntityY(room1,True)+(EntityY(Collider)-EntityY(room2,True)),EntityZ(room1,True)+(EntityZ(Collider)-EntityZ(room2,True)),True)
					ResetEntity Collider
					UpdateDoors()
					UpdateRooms()
				EndIf
				
				PlaySound2(ElevatorBeepSFX, Camera, room2, 4.0)				
			EndIf	
			
		EndIf
	EndIf
	
	Return State
	
End Function


;--------------------------------------- math -------------------------------------------------------

Function Distance#(x1#, y1#, x2#, y2#)
	Return(Sqr(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1))))
End Function

Function CurveValue#(number#, old#, smooth#)
	If number < old Then
		Return Max(old + (number - old) * (1.0 / smooth * FPSfactor), number)
	Else
		Return Min(old + (number - old) * (1.0 / smooth * FPSfactor), number)
	EndIf
End Function

Function CurveAngle#(val#, old#, smooth#)
   Local diff# = WrapAngle(val) - WrapAngle(old)
   If diff > 180 Then diff = diff - 360
   If diff < - 180 Then diff = diff + 360
   Return WrapAngle(old + (diff) * (1.0 / smooth * FPSfactor))
End Function

Function WrapAngle#(angle#)
	If angle = Infinity Then Return 0.0
	While angle < 0
		angle = angle + 360
	Wend 
	While angle >= 360
		angle = angle - 360
	Wend
	Return angle
End Function

Function GetAngle#(x1#, y1#, x2#, y2#)
	Return ATan2( y2 - y1, x2 - x1 )
End Function

Function CircleToLineSegIsect% (cx#, cy#, r#, l1x#, l1y#, l2x#, l2y#)
	
	;Palauttaa:
	;  True (1) kun:
	;      Ympyr‰ [keskipiste = (cx, cy): s‰de = r]
	;      leikkaa janan, joka kulkee pisteiden (l1x, l1y) & (l2x, l2y) kaitta
	;  False (0) muulloin
	
	;Ympyr‰n keskipisteen ja (ainakin toisen) janan p‰‰tepisteen et‰isyys < r
	;-> leikkaus
	If Distance(cx, cy, l1x, l1y) <= r Then
		Return True
	EndIf
	
	If Distance(cx, cy, l2x, l2y) <= r Then
		Return True
	EndIf	
	
	;Vektorit (janan vektori ja vektorit janan p‰‰tepisteist‰ ympyr‰n keskipisteeseen)
	Local SegVecX# = l2x - l1x
	Local SegVecY# = l2y - l1y
	
	Local PntVec1X# = cx - l1x
	Local PntVec1Y# = cy - l1y
	
	Local PntVec2X# = cx - l2x
	Local PntVec2Y# = cy - l2y
	
	;Em. vektorien pistetulot
	Local dp1# = SegVecX * PntVec1X + SegVecY * PntVec1Y
	Local dp2# = -SegVecX * PntVec2X - SegVecY * PntVec2Y
	
	;Tarkistaa onko toisen pistetulon arvo 0
	;tai molempien merkki sama
	If dp1 = 0 Or dp2 = 0 Then
	ElseIf (dp1 > 0 And dp2 > 0) Or (dp1 < 0 And dp2 < 0) Then
	Else
		;Ei kumpikaan -> ei leikkausta
		Return False
	EndIf
	
	;Janan p‰‰tepisteiden kautta kulkevan suoran ;yht‰lˆ; (ax + by + c = 0)
	Local a# = (l2y - l1y) / (l2x - l1x)
	Local b# = -1
	Local c# = -(l2y - l1y) / (l2x - l1x) * l1x + l1y
	
	;Ympyr‰n keskipisteen et‰isyys suorasta
	Local d# = Abs(a * cx + b * cy + c) / Sqr(a * a + b * b)
	
	;Ympyr‰ on liian kaukana
	;-> ei leikkausta
	If d > r Then Return False
	
	;Local kateetin_pituus# = Cos(angle) * hyp
	
	;Jos p‰‰st‰‰n t‰nne saakka, ympyr‰ ja jana leikkaavat (tai ovat sis‰kk‰in)
	Return True
End Function

Function Min#(a#, b#)
	If a < b Then
		Return a
	Else
		Return b
	EndIf
End Function

Function Max#(a#, b#)
	If a > b Then
		Return a
	Else
		Return b
	EndIf
End Function

;--------------------------------------- decals -------------------------------------------------------

Type Decals
	Field obj%
	Field SizeChange#, Size#, MaxSize#
	Field AlphaChange#, Alpha#
	Field blendmode%
	Field fx%
	Field ID%
	Field Timer#
	
	Field lifetime#
	
	Field x#, y#, z#
	Field pitch#, yaw#, roll#
End Type

Function CreateDecal.Decals(id%, x#, y#, z#, pitch#, yaw#, roll#)
	Local d.Decals = New Decals
	
	d\x = x
	d\y = y
	d\z = z
	d\pitch = pitch
	d\yaw = yaw
	d\roll = roll
	
	d\MaxSize = 1.0
	
	d\Alpha = 1.0
	d\Size = 1.0
	d\obj = CreateSprite()
	d\blendmode = 1
	
	EntityTexture(d\obj, DecalTextures(id))
	EntityFX(d\obj, 0)
	SpriteViewMode(d\obj, 2)
	PositionEntity(d\obj, x, y, z)
	RotateEntity(d\obj, pitch, yaw, roll)
	
	d\ID = id
	
	If DecalTextures(id) = 0 Or d\obj = 0 Then Return Null
	
	Return d
End Function

Function UpdateDecals()
	Local d.Decals
	For d.Decals = Each Decals
		If d\SizeChange <> 0 Then
			d\Size=d\Size + d\SizeChange * FPSfactor
			ScaleSprite(d\obj, d\Size, d\Size)
			
			Select d\ID
				Case 0
					If d\Timer <= 0 Then
						DebugLog "asdlkmgfldfkmgdflkmg"
						Local angle# = Rand(360)
						Local temp# = Rnd(d\Size)
						Local d2.Decals = CreateDecal(1, EntityX(d\obj) + Cos(angle) * temp, EntityY(d\obj) - 0.0005, EntityZ(d\obj) + Sin(angle) * temp, EntityPitch(d\obj), Rnd(360), EntityRoll(d\obj))
						d2\Size = Rnd(0.1, 0.5) : ScaleSprite(d2\obj, d2\Size, d2\Size)
						PlaySound2(DecaySFX(Rand(1, 3)), Camera, d2\obj, 10.0, Rnd(0.1, 0.5))
						;d\Timer = d\Timer + Rand(50,150)
						d\Timer = Rand(50, 100)
					Else
						d\Timer= d\Timer-FPSfactor
					End If
				;Case 6
				;	EntityBlend d\obj, 2
			End Select
			
			If d\Size >= d\MaxSize Then d\SizeChange = 0 : d\Size = d\MaxSize
		End If
		
		If d\AlphaChange <> 0 Then
			d\Alpha = Min(d\Alpha + FPSfactor * d\AlphaChange, 1.0)
			EntityAlpha(d\obj, d\Alpha)
		End If
		
		If d\lifetime > 0 Then
			d\lifetime=Max(d\lifetime-FPSfactor,5)
		EndIf
		
		If d\Size <= 0 Or d\Alpha <= 0 Or d\lifetime=5.0  Then
			FreeEntity(d\obj)
			Delete d
		End If
	Next
End Function


;--------------------------------------- INI-functions -------------------------------------------------------

Function GetINIString$(file$, section$, parameter$)
	Local TemporaryString$ = ""
	Local f% = ReadFile(file)
	
	While Not Eof(f)
		If Lower(ReadLine(f)) = "[" + Lower(section) + "]" Then
			Repeat
				TemporaryString = ReadLine(f)
			 	If Lower(Trim(Left(TemporaryString, Max(Instr(TemporaryString, "=") - 1, 0)))) = Lower(parameter) Then
					CloseFile f
					Return Trim( Right(TemporaryString,Len(TemporaryString)-Instr(TemporaryString,"=")) )
				EndIf
			Until Left(TemporaryString, 1) = "[" Or Eof(f)
			CloseFile f
			Return ""
		EndIf
	Wend
	
	CloseFile f
End Function

Function GetINIInt%(file$, section$, parameter$)
	Local txt$ = Trim(GetINIString(file$, section$, parameter$))
	If Lower(txt) = "true" Then
		Return 1
	ElseIf Lower(txt) = "false"
		Return 0
	Else
		Return Int(txt)
	EndIf
End Function

Function GetINIFloat#(file$, section$, parameter$)
	Return Float(GetINIString(file$, section$, parameter$))
End Function

Function PutINIValue%(file$, INI_sSection$, INI_sKey$, INI_sValue$)
	
	; Returns: True (Success) Or False (Failed)
	
	INI_sSection = "[" + Trim$(INI_sSection) + "]"
	Local INI_sUpperSection$ = Upper$(INI_sSection)
	INI_sKey = Trim$(INI_sKey)
	INI_sValue = Trim$(INI_sValue)
	Local INI_sFilename$ = file$
	
	; Retrieve the INI Data (If it exists)
		
	Local INI_sContents$ = INI_FileToString(INI_sFilename)
		
		; (Re)Create the INI file updating/adding the SECTION, KEY And VALUE
		
		Local INI_bWrittenKey% = False
		Local INI_bSectionFound% = False
		Local INI_sCurrentSection$ = ""
		
		Local INI_lFileHandle% = WriteFile(INI_sFilename)
		If INI_lFileHandle = 0 Then Return False ; Create file failed!
		
		Local INI_lOldPos% = 1
		Local INI_lPos% = Instr(INI_sContents, Chr$(0))
		
		While (INI_lPos <> 0)
			
			Local INI_sTemp$ = Mid$(INI_sContents, INI_lOldPos, (INI_lPos - INI_lOldPos))
			
			If (INI_sTemp <> "") Then
				
				If Left$(INI_sTemp, 1) = "[" And Right$(INI_sTemp, 1) = "]" Then
					
					; Process SECTION
					
					If (INI_sCurrentSection = INI_sUpperSection) And (INI_bWrittenKey = False) Then
						INI_bWrittenKey = INI_CreateKey(INI_lFileHandle, INI_sKey, INI_sValue)
					End If
					INI_sCurrentSection = Upper$(INI_CreateSection(INI_lFileHandle, INI_sTemp))
					If (INI_sCurrentSection = INI_sUpperSection) Then INI_bSectionFound = True
					
				Else
					DebugLog INI_sTemp
					If Left(INI_sTemp, 1) = ":" Then
						WriteLine INI_lFileHandle, INI_sTemp
					Else
						; KEY=VALUE				
						Local lEqualsPos% = Instr(INI_sTemp, "=")
						If (lEqualsPos <> 0) Then
							If (INI_sCurrentSection = INI_sUpperSection) And (Upper$(Trim$(Left$(INI_sTemp, (lEqualsPos - 1)))) = Upper$(INI_sKey)) Then
								If (INI_sValue <> "") Then INI_CreateKey INI_lFileHandle, INI_sKey, INI_sValue
								INI_bWrittenKey = True
							Else
								WriteLine INI_lFileHandle, INI_sTemp
							End If
						End If
					EndIf
					
				End If
				
			End If
			
			; Move through the INI file...
			
			INI_lOldPos = INI_lPos + 1
			INI_lPos% = Instr(INI_sContents, Chr$(0), INI_lOldPos)
			
		Wend
		
		; KEY wasn;t found in the INI file - Append a New SECTION If required And create our KEY=VALUE Line
			
			If (INI_bWrittenKey = False) Then
				If (INI_bSectionFound = False) Then INI_CreateSection INI_lFileHandle, INI_sSection
				INI_CreateKey INI_lFileHandle, INI_sKey, INI_sValue
			End If
			
			CloseFile INI_lFileHandle
			
			Return True ; Success
			
End Function

Function INI_FileToString$(INI_sFilename$)
	
	Local INI_sString$ = ""
	Local INI_lFileHandle%= ReadFile(INI_sFilename)
	If INI_lFileHandle <> 0 Then
		While Not(Eof(INI_lFileHandle))
			INI_sString = INI_sString + ReadLine$(INI_lFileHandle) + Chr$(0)
		Wend
		CloseFile INI_lFileHandle
	End If
	Return INI_sString
	
End Function

Function INI_CreateSection$(INI_lFileHandle%, INI_sNewSection$)
	
	If FilePos(INI_lFileHandle) <> 0 Then WriteLine INI_lFileHandle, "" ; Blank Line between sections
	WriteLine INI_lFileHandle, INI_sNewSection
	Return INI_sNewSection
	
End Function

Function INI_CreateKey%(INI_lFileHandle%, INI_sKey$, INI_sValue$)
	
	WriteLine INI_lFileHandle, INI_sKey + " = " + INI_sValue
	Return True
	
End Function


;--------------------------------------- MakeCollBox -functions -------------------------------------------------------

; Create a collision box For a mesh entity taking into account entity scale
	; (will Not work in non-uniform scaled space)
Function MakeCollBox(mesh%)
	Local sx# = EntityScaleX(mesh, 1)
	Local sy# = EntityScaleY(mesh, 1)
	Local sz# = EntityScaleZ(mesh, 1)
	GetMeshExtents(mesh)
	EntityBox mesh, Mesh_MinX * sx, Mesh_MinY * sy, Mesh_MinZ * sz, Mesh_MagX * sx, Mesh_MagY * sy, Mesh_MagZ * sz
End Function

; Find Mesh extents
Function GetMeshExtents(Mesh%)
	Local s%, surf%, surfs%, v%, verts%, x#, y#, z#
	Local minx# = Infinity
	Local miny# = Infinity
	Local minz# = Infinity
	Local maxx# = -Infinity
	Local maxy# = -Infinity
	Local maxz# = -Infinity
	
	surfs = CountSurfaces(Mesh)
	
	For s = 1 To surfs
		surf = GetSurface(Mesh, s)
		verts = CountVertices(surf)
		
		For v = 0 To verts - 1
			x = VertexX(surf, v)
			y = VertexY(surf, v)
			z = VertexZ(surf, v)
			
			If x < minx Then minx = x Else If x > maxx Then maxx = x
			If y < minx Then miny = y Else If y > maxy Then maxy = y
			If z < minz Then minz = z Else If z > maxz Then maxz = z
		Next
	Next
	
	Mesh_MinX = minx
	Mesh_MinY = miny
	Mesh_MinZ = minz
	Mesh_MaxX = maxx
	Mesh_MaxY = maxy
	Mesh_MaxZ = maxz
	Mesh_MagX = maxx-minx
	Mesh_MagY = maxy-miny
	Mesh_MagZ = maxz-minz
	
End Function

Function EntityScaleX#(entity%, globl% = False)
	If globl Then TFormVector 1, 0, 0, entity, 0 Else TFormVector 1, 0, 0, entity, GetParent(entity)
	Return Sqr(TFormedX() * TFormedX() + TFormedY() * TFormedY() + TFormedZ() * TFormedZ())
End Function 

Function EntityScaleY#(entity%, globl% = False)
	If globl Then TFormVector 0, 1, 0, entity, 0 Else TFormVector 0, 1, 0, entity, GetParent(entity)
	Return Sqr(TFormedX() * TFormedX() + TFormedY() * TFormedY() + TFormedZ() * TFormedZ())
End Function 

Function EntityScaleZ#(entity%, globl% = False)
	If globl Then TFormVector 0, 0, 1, entity, 0 Else TFormVector 0, 0, 1, entity, GetParent(entity)
	Return Sqr(TFormedX() * TFormedX() + TFormedY() * TFormedY() + TFormedZ() * TFormedZ())
End Function 



;~IDEal Editor Parameters:
;~F#F#D2#D6#263#332#34E#3C1#3CE#4F4#514#548#1693#16A6#1E8D#1E98#20F8#2111#212D#2145#2160
;~F#217D#2196#23B3#23C9#23FB#248D#24C6#24DB#2542#2546#254E#2555#2560#2564#259F#25A7#25B1#25C0#25DE#260D
;~F#2622#262D#2631#2684#2692#269A#26A6#26AF#26D5#26DA#26DF
;~C#Blitz3D