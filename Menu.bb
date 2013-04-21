;This file was edited with BLIde ( http://www.blide.org )

Global MenuBack% = LoadImage("GFX\menu\back.jpg")
Global MenuText% = LoadImage("GFX\menu\scptext.jpg")
Global Menu173% = LoadImage("GFX\menu\173back.jpg")
MenuWhite = LoadImage("GFX\menu\menuwhite.jpg")
MenuBlack = LoadImage("GFX\menu\menublack.jpg")
MaskImage MenuBlack, 255,255,0

Global MenuScale# = (GraphicHeight / 1024.0)

ResizeImage(MenuBack, ImageWidth(MenuBack) * MenuScale, ImageHeight(MenuBack) * MenuScale)
ResizeImage(MenuText, ImageWidth(MenuText) * MenuScale, ImageHeight(MenuText) * MenuScale)
ResizeImage(Menu173, ImageWidth(Menu173) * MenuScale, ImageHeight(Menu173) * MenuScale)

For i = 0 To 3
	ArrowIMG(i) = LoadImage("GFX\menu\arrow.png")
	RotateImage(ArrowIMG(i), 90 * i)
	HandleImage(ArrowIMG(i), 0, 0)
Next

Global RandomSeed$

Dim MenuBlinkTimer%(2), MenuBlinkDuration%(2)
MenuBlinkTimer%(0) = 1
MenuBlinkTimer%(1) = 1

Global MenuStr$, MenuStrX%, MenuStrY%

Global MainMenuTab%
Global SelectedMode%

Global IntroEnabled% = True

Global SelectedInputBox%

Global SavePath$ = "Saves\"

;nykyisen tallennuksen nimi ja samalla missä kansiossa tallennustiedosto sijaitsee saves-kansiossa
Global CurrSave$

Global SaveGameAmount%
Dim SaveGames$(SaveGameAmount+1) 
Dim SaveGameTime$(SaveGameAmount + 1)
Dim SaveGameDate$(SaveGameAmount + 1)

LoadSaveGames()


Dim KeyName$(211)
KeyName(1)="Esc"
For i = 2 To 10
	KeyName(i)=i-1
Next
KeyName(11)="0"
KeyName(12)="-"
KeyName(13)="="
KeyName(14)="Backspace"
KeyName(15)="Tab"
KeyName(16)="Q"
KeyName(17)="W"
KeyName(18)="E"
KeyName(19)="R"
KeyName(20)="T"
KeyName(21)="Y"
KeyName(22)="U"
KeyName(23)="I"
KeyName(24)="O"
KeyName(25)="P"
KeyName(26)="["
KeyName(27)="]"
KeyName(28)="Enter"
KeyName(29)="Left Ctrl"
KeyName(30)="A"
KeyName(31)="S"
KeyName(32)="D"
KeyName(33)="F"
KeyName(34)="G"
KeyName(35)="H"
KeyName(36)="J"
KeyName(37)="K"
KeyName(38)="L"
KeyName(39)=";"
KeyName(40)="'"
KeyName(42)="Left Shift"
KeyName(43)="\"
KeyName(44)="Z"
KeyName(45)="X"
KeyName(46)="C"
KeyName(47)="V"
KeyName(48)="B"
KeyName(49)="N"
KeyName(50)="M"
KeyName(51)=","
KeyName(52)="."
KeyName(54)="Right Shift"
KeyName(56)="Left Alt"
KeyName(57)="Space"
KeyName(58)="Caps Lock"
KeyName(59)="F1"
KeyName(60)="F2"
KeyName(61)="F3"
KeyName(62)="F4"
KeyName(63)="F5"
KeyName(64)="F6"
KeyName(65)="F7"
KeyName(66)="F8"
KeyName(67)="F9"
KeyName(68)="F10"
KeyName(157)="Right Control"
KeyName(184)="Right Alt"
KeyName(200)="Up"
KeyName(203)="Left"
KeyName(205)="Right"
KeyName(208)="Down"


Function UpdateMainMenu()
	Local x%, y%, width%, height%, temp%
	
	Cls
	
	ShowPointer()
	
	DrawImage(MenuBack, 0, 0)
	
	;for  i = 1 To bbCountGfxDrivers()
		;	DebugLog bbGfxDriverName(i)
	;Next
	
	If (MilliSecs() Mod MenuBlinkTimer(0)) >= Rand(MenuBlinkDuration(0)) Then
		DrawImage(Menu173, GraphicWidth - ImageWidth(Menu173), GraphicHeight - ImageHeight(Menu173))
	EndIf
		
	If Rand(300) = 1 Then
		MenuBlinkTimer(0) = Rand(4000, 8000)
		MenuBlinkDuration(0) = Rand(200, 500)
	End If
	
	SetFont Font1
	
	MenuBlinkTimer(1)=MenuBlinkTimer(1)-FPSfactor
	If MenuBlinkTimer(1) < MenuBlinkDuration(1) Then
		Color(50, 50, 50)
		Text(MenuStrX + Rand(-5, 5), MenuStrY + Rand(-5, 5), MenuStr, True)
		If MenuBlinkTimer(1) < 0 Then
			MenuBlinkTimer(1) = Rand(700, 800)
			MenuBlinkDuration(1) = Rand(10, 35)
			MenuStrX = Rand(700, 1000) * MenuScale
			MenuStrY = Rand(100, 600) * MenuScale
			
			Select Rand(0, 21)
				Case 0, 2, 3
					MenuStr = "DON'T BLINK"
				Case 4, 5
					MenuStr = "Secure. Contain. Protect."
				Case 6, 7, 8
					MenuStr = "You want happy endings? Fuck you."
				Case 9, 10, 11
					MenuStr = "Sometimes we would have had time to scream."
				Case 12, 19
					MenuStr = "NIL"
				Case 13
					MenuStr = "NO"
				Case 14
					MenuStr = "black white black white black white gray"
				Case 15
					MenuStr = "Stone does not care"
				Case 16
					MenuStr = "9341"
				Case 17
					MenuStr = "It controls the doors"
				Case 18
					MenuStr = "e8m106]af173o+079m895w914"
				Case 20
					MenuStr = "It has taken over everything"
				Case 21
					MenuStr = "Check out Fomalhauth"
			End Select
		EndIf
	EndIf
	
	SetFont Font2
	
	DrawImage(MenuText, GraphicWidth / 2 - ImageWidth(MenuText) / 2, GraphicHeight - 20 * MenuScale - ImageHeight(MenuText))
	
	If GraphicWidth > 1240 * MenuScale Then
		DrawTiledImageRect(MenuWhite, 0, 5, 512, 7 * MenuScale, 985.0 * MenuScale, 407.0 * MenuScale, (GraphicWidth - 1240 * MenuScale) + 300, 7 * MenuScale)
	EndIf
	
	If MainMenuTab = 0 Then
		For i% = 0 To 3
			temp = False
			x = 159 * MenuScale
			y = (286 + 100 * i) * MenuScale
			
			width = 400 * MenuScale
			height = 70 * MenuScale
			
			;color 255, 255, 255
			;rect(x, y, width, height)
			If MouseOn(x, y, width, height) Then
				;color(30, 30, 30)
				If MouseHit1 Then temp = True
			Else
				;color(0, 0, 0)
			EndIf
			
			Local txt$
			Select i
				Case 0
					txt = "NEW GAME"
					RandomSeed = ""
					If temp Then 
						If Rand(15)=1 Then 
							Select Rand(10)
								Case 1 
									RandomSeed = "NIL"
								Case 2
									RandomSeed = "NO"
								Case 3
									RandomSeed = "d9341"
								Case 4
									RandomSeed = "5CP_I73"
								Case 5
									RandomSeed = "DONTBLINK"
								Case 6
									RandomSeed = "CRUNCH"
								Case 7
									RandomSeed = "die"
								Case 8
									RandomSeed = "HTAED"
								Case 9
									RandomSeed = "rustledjim"
								Case 10
									RandomSeed = "larry"
							End Select
						Else
							n = Rand(4,8)
							For i = 1 To n
								If Rand(3)=1 Then
									RandomSeed = RandomSeed + Rand(0,9)
								Else
									RandomSeed = RandomSeed + Chr(Rand(97,122))
								EndIf
							Next							
						EndIf
						
						;RandomSeed = MilliSecs()
						MainMenuTab = 1
					EndIf
				Case 1
					txt = "LOAD GAME"
					If temp Then
						LoadSaveGames()
						MainMenuTab = 2
					EndIf
				Case 2
					txt = "MULTIPLAYER" ;MP mod
					If temp Then MainMenuTab = 4
				Case 3
					txt = "OPTIONS"
					If temp Then MainMenuTab = 3
				Case 3
					txt = "QUIT"
					If temp Then
						;DeInitExt
						End
					EndIf
			End Select
			
			DrawButton(x, y, width, height, txt)
			
			;rect(x + 4, y + 4, width - 8, height - 8)
			;color 255, 255, 255	
			;text(x + width / 2, y + height / 2, Str, True, True)
		Next	
		
	Else
		
		x = 159 * MenuScale
		y = 286 * MenuScale
		
		width = 400 * MenuScale
		height = 70 * MenuScale
		
		DrawFrame(x, y, width, height)
		
		If DrawButton(x + width + 20 * MenuScale, y, 580 * MenuScale - width - 20 * MenuScale, height, "BACK", False) Then 
			Select MainMenuTab
				Case 3
					PutINIValue(OptionFile, "options", "music volume", MusicVolume)
					PutINIValue(OptionFile, "options", "mouse sensitivity", MouseSens)
					PutINIValue(OptionFile, "options", "invert mouse y", InvertMouse)
					PutINIValue(OptionFile, "options", "bump mapping enabled", BumpEnabled)			
					PutINIValue(OptionFile, "options", "HUD enabled", HUDenabled)	
					
					PutINIValue(OptionFile, "options", "Right key", KEY_RIGHT)
					PutINIValue(OptionFile, "options", "Left key", KEY_LEFT)
					PutINIValue(OptionFile, "options", "Up key", KEY_UP)
					PutINIValue(OptionFile, "options", "Down key", KEY_DOWN)
					PutINIValue(OptionFile, "options", "Blink key", KEY_BLINK)
					PutINIValue(OptionFile, "options", "Sprint key", KEY_SPRINT)
					PutINIValue(OptionFile, "options", "Inventory key", KEY_INV)
					PutINIValue(OptionFile, "options", "Crouch key", KEY_CROUCH)
					
			End Select
			
			If MainMenuTab = 5 Then
				If mpState = 2 Then
					If MpStream Then
						WriteLine MpStream,"drop"
						DebugLog "Drop sent"
					EndIf
					For i% = 0 To MpNumClients - 1
						Delete MpClients(i%)
					Next
					MpNumClients = 0
				EndIf
				If mpState = 1 Then
					For i% = 1 To MpNumClients - 1
						WriteLine MpClients(i%)\stream,"serverClosed"
						Delete MpClients(i%)
					Next
					Delete MpClients(0)
					MpNumClients = 0
					CloseTCPServer MpConn
				EndIf
				MainMenuTab = 4
			Else
				MainMenuTab = 0
			EndIf
		EndIf
		
		Select MainMenuTab
			Case 1 ; New game
				
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 300 * MenuScale
				
				DrawFrame(x, y, width, height)
				
				x = 159 * MenuScale
				y = 286 * MenuScale
				
				width = 400 * MenuScale
				height = 70 * MenuScale
				
				Color(255, 255, 255)
				SetFont Font2
				Text(x + width / 2, y + height / 2, "NEW GAME", True, True)
				
				x = 160 * MenuScale
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 296 * MenuScale
				
				SetFont Font1
				
				Text (x + 20 * MenuScale, y + 20 * MenuScale, "Name:")
				CurrSave = InputBox(x + 100 * MenuScale, y + 15 * MenuScale, 200 * MenuScale, 30 * MenuScale, CurrSave, 1)
				CurrSave = Left(CurrSave, 15)
				
				
				Text (x + 20 * MenuScale, y + 60 * MenuScale, "Difficulty:")
				If DrawTick(x + 20 * MenuScale, y + 90 * MenuScale, (SelectedMode = 0)) Then SelectedMode = 0
				
				If DrawTick(x + 20 * MenuScale, y + 120 * MenuScale, (SelectedMode = 1)) Then SelectedMode = 1
				Color(255, 255, 255)
				Text(x + 60 * MenuScale, y + 90 * MenuScale, "EUCLID (normal saving)")
				Text(x + 60 * MenuScale, y + 120 * MenuScale, "KETER (permanent death)")
				
				
				Text(x + 20 * MenuScale, y + 160 * MenuScale, "Enable intro sequence:")
				IntroEnabled = DrawTick(x + 200 * MenuScale, y + 160 * MenuScale, IntroEnabled)
				
				Color 255,255,255
				Text (x + 20 * MenuScale, y + 200 * MenuScale, "Map seed:")
				RandomSeed = Left(InputBox(x+150*MenuScale, y+195*MenuScale, 200*MenuScale, 30*MenuScale, RandomSeed, 3),15)
				
				SetFont Font2
				
				If DrawButton(x + 420 * MenuScale, y + height + 20 * MenuScale, (580 - 400 - 20) * MenuScale, 70 * MenuScale, "START", False) Then
					If CurrSave <> "" Then
						If RandomSeed = "" Then
							RandomSeed = Abs(MilliSecs())
						EndIf
						Local strtemp$ = ""
						For i = 1 To Len(RandomSeed)
							strtemp = strtemp+Asc(Mid(strtemp,i,1))
						Next
						SeedRnd Abs(Int(strtemp))
						
						Local SameFound% = False
						For  i% = 1 To SaveGameAmount
							If SaveGames(i - 1) = CurrSave Then SameFound=SameFound+1
						Next
						
						If SameFound > 0 Then CurrSave = CurrSave + " (" + (SameFound + 1) + ")"
						
						LoadEntities()
						InitNewGame()
						MainMenuOpen = False
						FlushKeys()
						FlushMouse()
					Else
						
					End If
					
				EndIf
				
			Case 2 ;load game
				
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 300 * MenuScale
				
				DrawFrame(x, y, width, height)
				
				x = 159 * MenuScale
				y = 286 * MenuScale
				
				width = 400 * MenuScale
				height = 70 * MenuScale
				
				SetFont Font2				
				Color(255, 255, 255)
				SetFont Font2
				Text(x + width / 2, y + height / 2, "LOAD GAME", True, True)
				SetFont Font1
				
				x = 160 * MenuScale
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 296 * MenuScale
				
				If SaveGameAmount = 0 Then
					Text (x + 20 * MenuScale, y + 20 * MenuScale, "No saved games")
				Else
					x = x + 20 * MenuScale
					y = y + 20 * MenuScale
					For i% = 1 To SaveGameAmount
						Rect(x, y, (580 - 40)* MenuScale, 70* MenuScale)
						Color(0, 0, 0)
						Rect(x + 2 * MenuScale, y + 2 * MenuScale, (580 - 40 - 4) * MenuScale, (70 - 4) * MenuScale)
						Color(255, 255, 255)	
						;rect(x, y, 180, 80)	
						Text(x + 20 * MenuScale, y + 10 * MenuScale, SaveGames(i - 1))
						Text(x + 20 * MenuScale, y + (10+23) * MenuScale, SaveGameTime(i - 1))
						Text(x + 120 * MenuScale, y + (10+23) * MenuScale, SaveGameDate(i - 1))
						;text(x + 20, y + 30, ("Playing time: " + SaveGameTime[i - 1]))
						
						If DrawButton(x + 280 * MenuScale, y + 20 * MenuScale, 100 * MenuScale, 30 * MenuScale, "Load", False) Then
							LoadEntities()
							LoadGame(SavePath + SaveGames(i - 1) + "\")
							CurrSave = SaveGames(i - 1)
							InitLoadGame()
							MainMenuOpen = False
						EndIf
						
						If DrawButton(x + 400 * MenuScale, y + 20 * MenuScale, 100 * MenuScale, 30 * MenuScale, "Delete", False) Then
							DeleteFile(CurrentDir()+SavePath + SaveGames(i - 1)+"\save.txt")
							DeleteDir(CurrentDir()+SavePath + SaveGames(i - 1))
							DebugLog CurrentDir()+SavePath + SaveGames(i - 1)
							LoadSaveGames()
							Exit
						EndIf
						
						y=y+80 * MenuScale
					Next
				EndIf
				
			Case 4 ;MP mod
				
				If MpConn Then
					CloseTCPServer MpConn
				EndIf
				
				MpConn = 0
				MpStream = 0
				MpNumClient = 0
				
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 300 * MenuScale
				
				DrawFrame(x, y, width, height)
				
				x = 159 * MenuScale
				y = 286 * MenuScale
				
				width = 400 * MenuScale
				height = 70 * MenuScale
				
				SetFont Font2				
				Color(255, 255, 255)
				SetFont Font2
				Text(x + width / 2, y + height / 2, "MULTIPLAYER", True, True)
				SetFont Font1
				
				x = 160 * MenuScale
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 296 * MenuScale
				
				Text(x + 20 * MenuScale, y + 30 * MenuScale, "Name:")
				CurrName = InputBox(x + 75 * MenuScale, y + 20 * MenuScale, 300 * MenuScale, 30 * MenuScale, CurrName, 1)
				
				Text(x + 20 * MenuScale, y + 80 * MenuScale, "Port:")
				CurrPort = InputBox(x + 75 * MenuScale, y + 70 * MenuScale, 200 * MenuScale, 30 * MenuScale, CurrPort, 2)
				
				Text(x + 20 * MenuScale, y + 130 * MenuScale, "IP:")
				CurrIp = InputBox(x + 75 * MenuScale, y + 120 * MenuScale, 200 * MenuScale, 30 * MenuScale, CurrIp, 3)
				
				If DrawButton(x + 320 * MenuScale,  y + 70 * MenuScale, 130 * MenuScale, 30 * MenuScale, "Host", False) Then
					MpConn=CreateTCPServer(CurrPort)
					mpState = 1
					MainMenuTab = 5
					MpNumClients = 1
					MpMyID = 0
					MpClients(0) = New Client
					MpClients(0)\name = CurrName
					DebugLog "Hosting"
				EndIf
				
				If DrawButton(x + 320 * MenuScale,  y + 120 * MenuScale, 130 * MenuScale, 30 * MenuScale, "Connect", False) Then
					MpStream=OpenTCPStream(CurrIp,CurrPort)
					If Not MpStream Then
						mpState = 0
						MainMenuTab = 0
						DebugLog "Failed to create stream"
					Else
						WriteLine MpStream,CurrName
						
						avail = 0
						For i%=0 To 4000
							If ReadAvail(MpStream) Then
								avail = 1
								Exit
							EndIf
							Delay 1
						Next
						
						If avail Then
							msg$=ReadLine(MpStream)
							If msg$ = "pass"
								MainMenuTab = 5
								mpState = 2
								DebugLog "Connected"
								msg$ = ReadLine(MpStream)
								DebugLog "ClientNum: " + msg$
								For i% = 0 To Int%(msg)
									MpClients(i%) = New Client
									MpClients(i%)\name = ReadLine(MpStream)
									DebugLog "Rec: " + MpClients(i%)\name
								Next
								MpNumClients = Int%(msg)+1
								MpMyID = MpNumClients - 1
							Else
								mpState = 0
								MainMenuTab = 0
								DebugLog "Connection refused"
							EndIf
						Else
							mpState = 0
							MainMenuTab = 0
							DebugLog "Server not responding"
						EndIf
					EndIf
				EndIf
				
			Case 5 ;MP mod
				
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 300 * MenuScale
				
				DrawFrame(x, y, width, height)
				DrawFrame(x, y, width/3, height)
				
				x = 159 * MenuScale
				y = 286 * MenuScale
				
				width = 400 * MenuScale
				height = 70 * MenuScale
				
				SetFont Font2				
				Color(255, 255, 255)
				
				If mpState = 1 Then
					If MpConn Then
						MpStream = AcceptTCPStream(MpConn)
					EndIf
					If MpStream Then
						CurC% = MpNumClients
						MpClients(CurC) = New Client
						MpClients(CurC)\stream = MpStream
						msg$=ReadLine(MpStream)
						MpClients(CurC)\name=msg$
						WriteLine MpStream,"pass"
						WriteLine MpStream,MpNumClients
						For i% = 0 To MpNumClients
							WriteLine MpStream,MpClients(i%)\name
						Next
						
						For i% = 1 To MpNumClients - 1
							WriteLine MpClients(i%)\stream,"newclient"
							WriteLine MpClients(i%)\stream,msg$
						Next
						
						MpNumClients = MpNumClients+1
						DebugLog "New client: " + msg$
					EndIf
					
					For  i% = 1 To MpNumClients - 1
						If ReadAvail(MpClients(i%)\stream) Then
							msg$ = ReadLine(MpClients(i%)\stream)
							If msg$ = "drop" Then
								For j% = i% + 1 To MpNumClients - 1
									MpClients(j%-1)=MpClients(j%)
								Next
								;Delete MpClients(MpNumClients - 1)
								MpNumClients = MpNumClients - 1
								
								For i% = 1 To MpNumClients - 1
									WriteLine MpClients(i%)\stream,"dropclient"
									WriteLine MpClients(i%)\stream,i%
								Next 
							EndIf
						EndIf
					Next
					Text(x + width / 2, y + height / 2, "Hosting", True, True)
					
					If DrawButton(x + 420 * MenuScale, y + height + 340 * MenuScale, (580 - 400 - 20) * MenuScale, 70 * MenuScale, "START", False) Then
						If RandomSeed = "" Then
							RandomSeed = Abs(MilliSecs())
						EndIf
						
						;Local strtemp$ = ""
						For i = 1 To Len(RandomSeed)
							strtemp = strtemp+Asc(Mid(strtemp,i,1))
						Next
						SeedRnd Abs(Int(strtemp))
						
						;Local SameFound% = False
						For  i% = 1 To SaveGameAmount
							If SaveGames(i - 1) = CurrSave Then SameFound=SameFound+1
						Next
						
						If SameFound > 0 Then CurrSave = CurrSave + " (" + (SameFound + 1) + ")"
						
						For i% = 1 To MpNumClients - 1
							WriteLine MpClients(i%)\stream,"gamestart"
							WriteLine MpClients(i%)\stream,RandomSeed
							If i% = 1 Then
								MpClients(i%)\Obj = LoadAnimMesh("GFX\npcs\classd.b3d");
							Else
								MpClients(i%)\Obj = CopyEntity (MpClients(1)\Obj)
							EndIf
							tempS# = 0.5 / MeshWidth(MpClients(i%)\Obj)
							ScaleEntity MpClients(i%)\Obj, tempS, tempS, tempS
							
							MpClients(i%)\Collider = CreatePivot()
							EntityRadius MpClients(i%)\Collider, 0.32
							EntityType MpClients(i%)\Collider, HIT_PLAYER
						Next
						
						DebugLog "starting..."
						
						LoadEntities()
						InitNewGame()
						MainMenuOpen = False
						FlushKeys()
						FlushMouse()
					EndIf
				Else
					Text(x + width / 2, y + height / 2, "Connected", True, True)
					If MpStream Then
						If ReadAvail(MpStream) Then
							msg$ = ReadLine(MpStream)
							If msg$ = "newclient" Then
								msg$ = ReadLine(MpStream)
								MpClients(MpNumClients) = New Client
								MpClients(MpNumClients)\name = msg$
								MpNumClients = MpNumClients + 1
								DebugLog "New client: " + msg$
							EndIf
							If msg$ = "dropclient" Then
								msg$ = ReadLine(MpStream)
								i% = Int(msg$)
								For j% = i% + 1 To MpNumClients - 1
									MpClients(j%-1)=MpClients(j%)
								Next
								;Delete MpClients(MpNumClients - 1)
								MpNumClients = MpNumClients - 1
							EndIf
							If msg$ = "serverClosed" Then
								For j% = 0 To MpNumClients - 1
									Delete MpClients(j%)
								Next
								;Delete MpClients(MpNumClients - 1)
								MpNumClients = 0
								mpState = 0
								MainMenuTab = 0
								CloseTCPStream MpStream
								DebugLog "Server closed."
							EndIf
							If msg$ = "gamestart"
								RandomSeed = ReadLine(MpStream)
								;Local strtemp$ = ""
								For i = 1 To Len(RandomSeed)
									strtemp = strtemp+Asc(Mid(strtemp,i,1))
								Next
								SeedRnd Abs(Int(strtemp))
								
								;Local SameFound% = False
								For  i% = 1 To SaveGameAmount
									If SaveGames(i - 1) = CurrSave Then SameFound=SameFound+1
								Next
								
								If SameFound > 0 Then CurrSave = CurrSave + " (" + (SameFound + 1) + ")"
								
								For i% = 0 To MpNumClients - 1
									If i% <> MpMyID Then
										If i% = 0 Then
											MpClients(i%)\Obj = LoadAnimMesh("GFX\npcs\classd.b3d");
										Else
											MpClients(i%)\Obj = CopyEntity (MpClients(0)\Obj)
										EndIf
										tempS# = 0.5 / MeshWidth(MpClients(i%)\Obj)
										ScaleEntity MpClients(i%)\Obj, tempS, tempS, tempS
										
										MpClients(i%)\Collider = CreatePivot()
										EntityRadius MpClients(i%)\Collider, 0.32
										EntityType MpClients(i%)\Collider, HIT_PLAYER
									EndIf
								Next
								DebugLog(Mpnumclients)
								
								LoadEntities()
								InitNewGame()
								MainMenuOpen = False
								FlushKeys()
								FlushMouse()
							EndIf
						EndIf
					EndIf
				EndIf
				
				SetFont Font1
				
				x = 160 * MenuScale
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 296 * MenuScale
				
				
				
				For i% = 0 To MpNumClients-1
					Text(x + 20 * MenuScale, y + 30 * MenuScale + i% * 30 * MenuScale, MpClients(i%)\name)
				Next	
				
			Case 3
				
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 350 * MenuScale
				
				DrawFrame(x, y, width, height)
				
				x = 159 * MenuScale
				y = 286 * MenuScale
				
				width = 400 * MenuScale
				height = 70 * MenuScale
				
				Color(255, 255, 255)
				SetFont Font2
				Text(x + width / 2, y + height / 2, "OPTIONS", True, True)
				SetFont Font1
				
				x = 160 * MenuScale
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 350 * MenuScale
				
				SetFont Font1
				
				Text (x + 20 * MenuScale, y + 20 * MenuScale, "Mouse sensitivity:")
				
				MouseSens = (SlideBar(x + 240*MenuScale, y+20*MenuScale, 150*MenuScale, (MouseSens+0.5)*100.0)/100.0)-0.5
				Color 170,170,170
				Text (x + 180 * MenuScale, y + 20 * MenuScale, "LOW")							
				Text (x + 426 * MenuScale, y + 20 * MenuScale, "HIGH")		
				
				Color 255,255,255				
				Text (x + 20 * MenuScale, y + 60 * MenuScale, "Invert mouse Y-axis:")	
				InvertMouse = DrawTick(x + 200 * MenuScale, y + 58 * MenuScale, InvertMouse)
				
				Color 255,255,255
				Text (x + 20 * MenuScale, y + 100 * MenuScale, "Music volume:")		
				MusicVolume = (SlideBar(x + 240*MenuScale, y+100*MenuScale, 150*MenuScale, MusicVolume*100.0)/100.0)
				Color 170,170,170 
				Text (x + 180 * MenuScale, y + 100 * MenuScale, "LOW")							
				Text (x + 426 * MenuScale, y + 100 * MenuScale, "HIGH")	
				
				Color 255,255,255				
				Text (x + 20 * MenuScale, y + 140 * MenuScale, "Enable HUD:")	
				HUDenabled = DrawTick(x + 200 * MenuScale, y + 138 * MenuScale, HUDenabled)		
				
				Color 255,255,255				
				Text (x + 20 * MenuScale, y + 180 * MenuScale, "Enable bump mapping:")	
				BumpEnabled = DrawTick(x + 200 * MenuScale, y + 178 * MenuScale, BumpEnabled)		
				
				Text (x + 20 * MenuScale, y + 220 * MenuScale, "Control configuration:")	
				
				Text (x + 20 * MenuScale, y + 240 * MenuScale, "Up")
				InputBox(x + 170 * MenuScale, y + 240 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_UP,210)),5)		
				Text (x + 20 * MenuScale, y + 260 * MenuScale, "Left")
				InputBox(x + 170 * MenuScale, y + 260 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_LEFT,210)),3)	
				Text (x + 20 * MenuScale, y + 280 * MenuScale, "Down")
				InputBox(x + 170 * MenuScale, y + 280 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_DOWN,210)),6)				
				Text (x + 20 * MenuScale, y + 300 * MenuScale, "Right")
				InputBox(x + 170 * MenuScale, y + 300 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_RIGHT,210)),4)	
				
				Text (x + 300 * MenuScale, y + 240 * MenuScale, "Blink")
				InputBox(x + 450 * MenuScale, y + 240 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_BLINK,210)),7)				
				Text (x + 300 * MenuScale, y + 260 * MenuScale, "Sprint")
				InputBox(x + 450 * MenuScale, y + 260 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_SPRINT,210)),8)
				Text (x + 300 * MenuScale, y + 280 * MenuScale, "Inventory")
				InputBox(x + 450 * MenuScale, y + 280 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_INV,210)),9)
				Text (x + 300 * MenuScale, y + 300 * MenuScale, "Crouch")
				InputBox(x + 450 * MenuScale, y + 300 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_CROUCH,210)),10)
				
				For i = 0 To 227
					If KeyHit(i) Then key = i : Exit
				Next
				If key<>0 Then
					DebugLog key
					Select SelectedInputBox
						Case 3
							KEY_LEFT = key
						Case 4
							KEY_RIGHT = key
						Case 5
							KEY_UP = key
						Case 6
							KEY_DOWN = key
						Case 7
							KEY_BLINK = key
						Case 8
							KEY_SPRINT = key
						Case 9
							KEY_INV = key
						Case 10
							KEY_CROUCH = key
					End Select
					SelectedInputBox = 0
				EndIf
		End Select
		
	End If
	
	;DrawTiledImageRect(MenuBack, 985 * MenuScale, 860 * MenuScale, 200 * MenuScale, 20 * MenuScale, 1200 * MenuScale, 866 * MenuScale, 300, 20 * MenuScale)
	
	If Fullscreen Then DrawImage CursorIMG, MouseX(),MouseY()
	
	SetFont Font1
End Function

Function UpdateLauncher()
	DebugLog LauncherWidth + ", " + LauncherHeight
	
	MenuScale = 1
	
	Graphics3D(LauncherWidth, LauncherHeight, 0, 2)
	InitExt
	
	SetBuffer BackBuffer()
	
	Font1 = LoadFont("GFX\TI-83.ttf", 18)
	MenuWhite = LoadImage("GFX\menu\menuwhite.jpg")
	MenuBlack = LoadImage("GFX\menu\menublack.jpg")	
	MaskImage MenuBlack, 255,255,0
	LauncherIMG = LoadImage("GFX\menu\launcher.jpg")
	ButtonSFX% = LoadSound("SFX\button.ogg")
	Local i%	
	
	For i = 0 To 3
		ArrowIMG(i) = LoadImage("GFX\menu\arrow.png")
		RotateImage(ArrowIMG(i), 90 * i)
		HandleImage(ArrowIMG(i), 0, 0)
	Next
	
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
	
	Repeat
		Cls
		
		MouseHit1 = MouseHit(1)
		
		Color 255, 255, 255
		;rect(20, 20, LauncherWidth - 40, 200)
		DrawImage(LauncherIMG, 0, 0)
		
		;DrawTiledImageRect(MenuWhite, 0, 0, 512, 512, 20, 20, LauncherWidth - 40, 200)
		;rect(20, 260, 430, LauncherHeight - 60 - 260)
		
		Text(20, 240 - 65, "Resolution: ")
		
		Local x% = 40
		Local y% = 280 - 65
		For i = 0 To (GFXModes - 1)
			Color 0, 0, 0
			If SelectedGFXMode = i Then Rect(x - 1, y - 1, 100, 20, False)
			
			Text(x, y, (GfxModeWidths(i) + "x" + GfxModeHeights(i)))
			If MouseOn(x - 1, y - 1, 100, 20) Then
				Color 100, 100, 100
				Rect(x - 1, y - 1, 100, 20, False)
				If MouseHit1 Then SelectedGFXMode = i
			EndIf
			
			y=y+20
			If y >= 240 - 65 + (LauncherHeight - 80 - 260) Then y = 280 - 65 : x=x+100
		Next
		
		;-----------------------------------------------------------------
		Color 255, 255, 255
		x = 30
		y = 369
		Rect(x - 10, y, 240, 95)
		Text(x - 10, y - 25, "Graphics:")
		
		y=y+10
		For i = 1 To CountGfxDrivers()
			Color 0, 0, 0
			If SelectedGFXDriver = i Then Rect(x - 1, y - 1, 190, 20, False)
			;text(x, y, bbGfxDriverName(i))
			LimitText(GfxDriverName(i), x, y, 190)
			If MouseOn(x - 1, y - 1, 190, 20) Then
				Color 100, 100, 100
				Rect(x - 1, y - 1, 190, 20, False)
				If MouseHit1 Then SelectedGFXDriver = i
			EndIf
			
			y=y+20
		Next
		
		;-----------------------------------------------------------------		
		;Color 255, 255, 255
		;x = 30 + 250
		;y = 369
		;Rect(x - 10, y, 230, 95)
		;Text(x - 10, y - 25, "Audio:")
		
		;y=y+10
		;For i = ScrollAudioDriver To AudioDriver.length - 1
			
		;	Color 0, 0, 0
		;	If SelectedAudioDriver = i Then Rect(x - 1, y - 1, 180, 20, False)
		;	;text(x, y, AudioDriver(i))
		;	LimitText(AudioDriver(i), x, y, 180)
		;	If MouseOn(x - 1, y - 1, 180, 20) Then
		;		Color 100, 100, 100
		;		Rect(x - 1, y - 1, 180, 20, False)
		;		If MouseHit1 Then SelectedAudioDriver = i
		;	EndIf
		;	
		;	y=y+20
		;	
		;	If y > 369 + 80 Then Exit
		;Next
		
		;If AudioDriver.Length > 4 Then
		;	If ScrollAudioDriver > 0 Then
		;		If DrawButton(x + 190, 369, 30, 48, "", False) Then ScrollAudioDriver=ScrollAudioDriver-1
		;		DrawImage(ArrowIMG(0), x + 190 + 5, 369 + 14)
		;	EndIf
		;	If ScrollAudioDriver + 4 < AudioDriver.Length Then
		;		If DrawButton(x + 190, 369 + 48, 30, 47, "", False) Then ScrollAudioDriver=ScrollAudioDriver+1
		;		DrawImage(ArrowIMG(2), x + 190 + 5, 369 + 48 + 14)
		;	End If			
		;EndIf
		
		Fullscreen = DrawTick(40 + 430, 262 - 55, Fullscreen)
		LauncherEnabled = DrawTick(40 + 430, 260 - 55 + 40, LauncherEnabled)
		
		Color 255, 255, 255
		Text(40 + 430 + 35, 262 - 55, "Fullscreen")
		Text(40 + 430 + 35, 262 - 55 + 40, "Use launcher")
		
		If DrawButton(LauncherWidth - 30 - 90, LauncherHeight - 50 - 55, 100, 30, "LAUNCH", False) Then
			GraphicWidth =GfxModeWidths(SelectedGFXMode)
			GraphicHeight = GfxModeHeights(SelectedGFXMode)
			Exit
		EndIf
		
		If DrawButton(LauncherWidth - 30 - 90, LauncherHeight - 50, 100, 30, "EXIT", False) Then End
		Flip
	Forever
	
	PutINIValue(OptionFile, "options", "width", GfxModeWidths(SelectedGFXMode))
	PutINIValue(OptionFile, "options", "height", GfxModeHeights(SelectedGFXMode))
	If Fullscreen Then
		PutINIValue(OptionFile, "options", "fullscreen", "true")
	Else
		PutINIValue(OptionFile, "options", "fullscreen", "false")
	EndIf
	If LauncherEnabled Then
		PutINIValue(OptionFile, "launcher", "launcher enabled", "true")
	Else
		PutINIValue(OptionFile, "launcher", "launcher enabled", "false")
	EndIf
	
	PutINIValue(OptionFile, "options", "audio driver", SelectedAudioDriver)
	PutINIValue(OptionFile, "options", "gfx driver", SelectedGFXDriver)
	
End Function


Function DrawTiledImageRect(img%, srcX%, srcY%, srcwidth#, srcheight#, x%, y%, width%, height%)
	
	Local x2% = x
	While x2 < x+width
		Local y2% = y
		While y2 < y+height
			If x2 + srcwidth > x + width Then srcwidth = srcwidth - Max((x2 + srcwidth) - (x + width), 1)
			If y2 + srcheight > y + height Then srcheight = srcheight - Max((y2 + srcheight) - (y + height), 1)
			DrawImageRect(img, x2, y2, srcX, srcY, srcwidth, srcheight)
			y2 = y2 + srcheight
		Wend
		x2 = x2 + srcwidth
	Wend
	
End Function



Type LoadingScreens
	Field imgpath$
	Field img%
	Field ID%
	Field title$
	Field alignx%, aligny%
	Field disablebackground%
	Field txt$[5], txtamount%
End Type

Function InitLoadingScreens(file$)
	Local TemporaryString$, Temp%, i%, n%
	Local rt.RoomTemplates = Null
	Local StrTemp$ = ""
	
	Local f = OpenFile(file)
	
	While Not Eof(f)
		TemporaryString = Trim(ReadLine(f))
		If Left(TemporaryString,1) = "[" Then
			TemporaryString = Mid(TemporaryString, 2, Len(TemporaryString) - 2)
			
			ls.loadingscreens = New LoadingScreens
			LoadingScreenAmount=LoadingScreenAmount+1
			ls\id = LoadingScreenAmount
			
			ls\title = TemporaryString
			ls\imgpath = GetINIString(file, TemporaryString, "image path")
			
			For i = 0 To 4
				ls\txt[i] = GetINIString(file, TemporaryString, "text"+(i+1))
				If ls\txt[i]<> "" Then ls\txtamount=ls\txtamount+1
			Next
			
			ls\disablebackground = GetINIInt(file, TemporaryString, "disablebackground")
			
			Select Lower(GetINIString(file, TemporaryString, "align x"))
				Case "left"
					ls\alignx = -1
				Case "middle", "center"
					ls\alignx = 0
				Case "right" 
					ls\alignx = 1
			End Select 
			
			Select Lower(GetINIString(file, TemporaryString, "align y"))
				Case "top", "up"
					ls\aligny = -1
				Case "middle", "center"
					ls\aligny = 0
				Case "bottom", "down"
					ls\aligny = 1
			End Select 			
			
		EndIf
	Wend
	
	CloseFile f
End Function



Function DrawLoading(percent%, shortloading=False)
	
	Local x%, y%
	
	;If percent = 0 Then
	;	If Rand(10) = 1 Then DrawCwm = True Else DrawCwm = False
	;EndIf
	
	
	If percent = 0 Then
		LoadingScreenText=0
		For ls.loadingscreens = Each LoadingScreens
			If ls\img <> 0 Then FreeImage ls\img : ls\img = 0
		Next			
		
		temp = Rand(1,LoadingScreenAmount)
		For ls.loadingscreens = Each LoadingScreens
			If ls\id = temp Then 
				DebugLog ls\title
				DebugLog ls\Txt[0]
				DebugLog ls\Txt[1]
				SelectedLoadingScreen = ls 
				ls\img = LoadImage("Loadingscreens\"+ls\imgpath)
				DebugLog "Loadingscreens\"+ls\imgpath
				Exit
			EndIf
		Next
	EndIf	
	
	Color 255, 255, 255
	
	firstloop = True
	Repeat 
		
		Cls
		;Cls(True,False)
		
		If shortloading = False Then
			If percent > (100.0 / SelectedLoadingScreen\txtamount)*(LoadingScreenText+1) Then
				LoadingScreenText=LoadingScreenText+1
			EndIf
		EndIf
		
		If (Not SelectedLoadingScreen\disablebackground) Then
			DrawImage LoadingBack, GraphicWidth/2 - ImageWidth(LoadingBack)/2, GraphicHeight/2 - ImageHeight(LoadingBack)/2
		EndIf	
			
		If SelectedLoadingScreen\alignx = 0 Then
			x = GraphicWidth/2 - ImageWidth(SelectedLoadingScreen\img)/2 
		ElseIf  SelectedLoadingScreen\alignx = 1
			x = GraphicWidth - ImageWidth(SelectedLoadingScreen\img)
		Else
			x = 0
		EndIf
		
		If SelectedLoadingScreen\aligny = 0 Then
			y = GraphicHeight/2 - ImageHeight(SelectedLoadingScreen\img)/2 
		ElseIf  SelectedLoadingScreen\aligny = 1
			y = GraphicHeight - ImageHeight(SelectedLoadingScreen\img)
		Else
			y = 0
		EndIf	
		
		DrawImage SelectedLoadingScreen\img, x, y
		
		
		If SelectedLoadingScreen\title = "CWM" Then
			SetFont Font2
			strtemp$ = ""
			temp = Rand(2,9)
			For i = 0 To temp
				strtemp$ = STRTEMP + Chr(Rand(130,250))
			Next
			Text(GraphicWidth / 2, GraphicHeight / 2 + 80, strtemp, True, True)
			
			If percent = 0 Then 
				Select Rand(10)
					Case 1
						SelectedLoadingScreen\txt[0] ="A very fine radio might prove to be useful"
					Case 2
						SelectedLoadingScreen\txt[0] ="ThIS PLaCE WiLL BUrN"
					Case 3
						SelectedLoadingScreen\txt[0] ="You can't control it"
					Case 4
						SelectedLoadingScreen\txt[0] ="eof9nsd3jue4iwe1fgj"
					Case 5
						SelectedLoadingScreen\txt[0] = "YOU NEED TO TRUST IT"
					Case 6,7,8
						SelectedLoadingScreen\txt[0] = "???____??_???__????n?"	
					Case 9,10
						SelectedLoadingScreen\txt[0] = "???????????"				
				End Select
			EndIf
			
			strtemp$ = SelectedLoadingScreen\txt[0]
			temp = Int(Len(SelectedLoadingScreen\txt[0])-Rand(5))
			For i = 0 To Rand(10,15);temp
				strtemp$ = Replace(SelectedLoadingScreen\txt[0],Mid(SelectedLoadingScreen\txt[0],Rand(1,Len(strtemp)-1),1),Chr(Rand(130,250)))
			Next		
			SetFont Font1
			RowText(strtemp, GraphicWidth / 2-200, GraphicHeight / 2 +120,400,300,True)		
		Else
			
			Color 0,0,0
			SetFont Font2
			Text(GraphicWidth / 2 + 1, GraphicHeight / 2 + 80 + 1, SelectedLoadingScreen\title, True, True)
			SetFont Font1
			RowText(SelectedLoadingScreen\txt[LoadingScreenText], GraphicWidth / 2-200+1, GraphicHeight / 2 +120+1,400,300,True)
			
			Color 255,255,255
			SetFont Font2
			Text(GraphicWidth / 2, GraphicHeight / 2 +80, SelectedLoadingScreen\title, True, True)
			SetFont Font1
			RowText(SelectedLoadingScreen\txt[LoadingScreenText], GraphicWidth / 2-200, GraphicHeight / 2 +120,400,300,True)
			
		EndIf
		
		Color 0,0,0
		Text(GraphicWidth / 2 + 1, GraphicHeight / 2 - 100 + 1, "LOADING - " + percent + " %", True, True)
		Color 255,255,255
		Text(GraphicWidth / 2, GraphicHeight / 2 - 100, "LOADING - " + percent + " %", True, True)
		;If DrawCwm Then DrawImage CwmImg, GraphicWidth - 380, GraphicHeight - 525
		
		Local width% = 300, height% = 20
		x% = GraphicWidth / 2 - width / 2
		y% = GraphicHeight / 2 + 30 - 100
		
		Rect(x, y, width+4, height, False)
		For  i% = 1 To Int((width - 2) * (percent / 100.0) / 10)
			DrawImage(BlinkMeterIMG, x + 3 + 10 * (i - 1), y + 3)
		Next
		
		If percent = 99 Then 
			Text(GraphicWidth / 2, GraphicHeight / 2 + 100, "Waiting for players...", True, False)
		EndIf
		
		If percent = 100 Then 
			If firstloop Then PlaySound HorrorSFX(8)
			Text(GraphicWidth / 2, GraphicHeight - 50, "PRESS ANY KEY", True, True)
		Else
			FlushKeys()
			FlushMouse()
		EndIf
		
		Flip
		
		firstloop = False
		If percent <> 100 Then Exit
		
	Until (GetKey()<>0 Or MouseHit(1))
End Function



Function rInput$(aString$)
	Local value% = GetKey()
	Local length% = Len(aString$)
	
	If value = 8 Then
		value = 0
		If length > 0 Then aString$ = Left(aString, length - 1)
	EndIf
	
	If value = 13 Or value = 0 Then
		Return aString$
	ElseIf value > 0 And value < 7 Or value > 26 And value < 32 Or value = 9
		Return aString$
	Else
		aString$ = aString$ + Chr(value)
		Return aString$
	End If
End Function

Function InputBox$(x%, y%, width%, height%, Txt$, ID% = 0)
	;TextBox(x,y,width,height,Txt$)
	Color (255, 255, 255)
	Rect(x, y, width, height)
	Color (0, 0, 0)
	Rect(x + 2, y + 2, width - 4, height - 4)
	
	Color (255, 255, 255)
	Text(x + width / 2, y + height / 2, Txt, True, True)
	
	Local MouseOnBox% = False
	If MouseOn(x, y, width, height) Then
		MouseOnBox = True
		If MouseHit1 Then SelectedInputBox = ID : FlushKeys
	EndIf
	
	If (Not MouseOnBox) And MouseHit1 And SelectedInputBox = ID Then SelectedInputBox = 0
	
	If SelectedInputBox = ID Then
		Txt = rInput(Txt)
		If (MilliSecs() Mod 800) < 400 Then Rect (x + width / 2 + StringWidth(Txt) / 2 + 2, y + height / 2 - 5, 2, 12)
	EndIf
	
	Return Txt
End Function

Function DrawFrame(x%, y%, width%, height%)
	Color 255, 255, 255
	DrawTiledImageRect(MenuWhite, 0, (y Mod 256), 512, 512, x, y, width, height)
	
	DrawTiledImageRect(MenuBlack, 0, (y Mod 256), 512, 512, x+4, y+4, width-8, height-8)	
	;Color(0, 0, 0)
	;Rect(x + 4, y + 4, width - 8, height - 8)
End Function

Function DrawButton%(x%, y%, width%, height%, txt$, bigfont% = True)
	Local clicked% = False
	
	DrawFrame (x, y, width, height)
	If MouseOn(x, y, width, height) Then
		Color(30, 30, 30)
		If MouseHit1 Then clicked = True : PlaySound(ButtonSFX)
		Rect(x + 4, y + 4, width - 8, height - 8)	
	Else
		Color(0, 0, 0)
	EndIf
	
	Color (255, 255, 255)
	If bigfont Then SetFont Font2 Else SetFont Font1
	Text(x + width / 2, y + height / 2, txt, True, True)
	
	Return clicked
End Function

Function DrawTick%(x%, y%, selected%, locked% = False)
	Local width% = 20 * MenuScale, height% = 20 * MenuScale
	
	Color (255, 255, 255)
	Rect(x, y, width, height)
	
	Local Highlight% = MouseOn(x, y, width, height) And (Not locked)
	
	If Highlight Then
		Color(50, 50, 50)
		If MouseHit1 Then selected = (Not selected) : PlaySound (ButtonSFX)
	Else
		Color(0, 0, 0)		
	End If
	
	Rect(x + 2, y + 2, width - 4, height - 4)
	
	If selected Then
		If Highlight Then
			Color 255,255,255
		Else
			Color 200,200,200
		EndIf
		
		Rect(x + 4, y + 4, width - 8, height - 8)
	EndIf
	
	Color 255, 255, 255
	
	Return selected
End Function

Function SlideBar#(x%, y%, width%, value#)
	
	If MouseDown1 Then
		If MouseX() >= x And MouseX() <= x + width + 14 And MouseY() >= y And MouseY() <= y + 20 Then
			value = Min(Max((MouseX() - x) * 100 / width, 0), 100)
		EndIf
	EndIf
	
	Color 255,255,255
	Rect(x, y, width + 14, 20,False)
	
	DrawImage(BlinkMeterIMG, x + width * value / 100.0 +3, y+3)
	
	Return value
	
End Function




Function RowText(A$, X, Y, W, H, align% = 0, Leading = 0)
	;Display A$ starting at X,Y - no wider than W And no taller than H (all in pixels).
	;Leading is optional extra vertical spacing in pixels
	Local LinesShown = 0
	Local Height = StringHeight(A$) + Leading
	Local b$
	
	While Len(A) > 0
		Local space = Instr(A$, " ")
		If space = 0 Then space = Len(A$)
		Local temp$ = Left(A$, space)
		Local trimmed$ = Trim(temp) ;we might ignore a final space 
		Local extra = 0 ;we haven't ignored it yet
		;ignore final space If doing so would make a word fit at End of Line:
		If (StringWidth (b$ + temp$) > W) And (StringWidth (b$ + trimmed$) <= W) Then
			temp = trimmed
			extra = 1
		EndIf
		
		If StringWidth (b$ + temp$) > W Then ;too big, so Print what will fit
			If align Then
				Text(X + W / 2 - (StringWidth(b) / 2), LinesShown * Height + Y, b)
			Else
				Text(X, LinesShown * Height + Y, b)
			EndIf			
			
			LinesShown = LinesShown + 1
			b$=""
		Else ;append it To b$ (which will eventually be printed) And remove it from A$
			b$ = b$ + temp$
			A$ = Right(A$, Len(A$) - (Len(temp$) + extra))
		EndIf
		
		If ((LinesShown + 1) * Height) > H Then Exit ;the Next Line would be too tall, so leave
	Wend
	
	If (b$ <> "") And((LinesShown + 1) <= H) Then
		If align Then
			Text(X + W / 2 - (StringWidth(b) / 2), LinesShown * Height + Y, b) ;Print any remaining Text If it'll fit vertically
		Else
			Text(X, LinesShown * Height + Y, b) ;Print any remaining Text If it'll fit vertically
		EndIf
	EndIf
					
End Function

Function LimitText%(txt$, x%, y%, width%)
	If txt = "" Or width = 0 Then Return 0
	Local TextLength% = StringWidth(txt)
	Local UnFitting% = TextLength - width
	If UnFitting <= 0 Then ;mahtuu
		Text(x, y, txt)
	Else ;ei mahdu
		Local LetterWidth% = TextLength / Len(txt)
			
		Text(x, y, Left(txt, Max(Len(txt) - UnFitting / LetterWidth - 4, 1)) + "...")
	End If
End Function










Function alpha_precalc_image(img)
	
	imgwidth	= ImageWidth(img) - 1
	imgheight	= ImageHeight(img) - 1
	
	framesize = (imgwidth + 1) * (imgheight + 1) * 4		; no of bytes pr. 'frame'
	
	bsize = (framesize * 101) + 8					; banksize + little extra (until i bother debugging)
	
	bank = CreateBank(bsize)
	
	PokeShort bank,0,imgwidth
	PokeShort bank,2,imgheight
	
	pointer = 4	
	
	SetBuffer ImageBuffer(img)
	
	LockBuffer ImageBuffer(img)
	
	For z# = 0 To 1 Step 0.01
		
		For y = 0 To (imgheight)
			
			For x = 0 To (imgwidth)
				
				col		= (ReadPixelFast(x,y) And $FFFFFF)
				r		= Int(((col And $FF0000) Shr 16) * z#)
				g		= Int(((col And $FF00) Shr 8) * z#)
				b		= Int((col And $FF) * z#)
				
				lum		= r
				
				If g > lum
					
					lum = g
					
				ElseIf b > lum
					
					lum = b
					
				EndIf
				
				PokeByte bank,pointer,r
				PokeByte bank,pointer + 1,g
				PokeByte bank,pointer + 2,b
				PokeByte bank,pointer + 3,lum 
				
				pointer = pointer + 4
				
			Next
			
		Next
		
	Next
	
	UnlockBuffer ImageBuffer(img)
	
	FreeImage(img)
	
	SetBuffer BackBuffer()
	
	Return bank
	
End Function

Function alpha_render_image(bank,scrx,scry,alpha#)
	
	imgwidth	= PeekShort(bank,0)
	imgheight	= PeekShort(bank,2)
	
	scrx = scrx - (imgwidth / 2)
	scry = scry - (imgheight / 2)
	
	framesize	= (imgwidth + 1) * (imgheight + 1) * 4
	
	frameno	= Int(alpha# * 100)
	
	pointer = (framesize * frameno) + 4
	
	destalpha# = 1 - alpha#
	
	If alpha# > 1 Then alpha# = 1
	If alpha# < 0 Then alpha# = 0
	
	LockBuffer BackBuffer()
	
	For y = 0 To (imgheight)
		
		For x = 0 To (imgwidth)
			
			sx = scrx + x
			sy = scry + y
			
			If sx > 0 And sx < (scrw - 1) And sy > 0 And sy < (scrh - 1)
				
				dcol = (ReadPixelFast(sx,sy) And $FFFFFF)
				
				If dcol < $FFFFFF
					
					lum	= PeekByte(bank,pointer + 3)
					
					fr	= ((dcol And $FF0000) Shr 16) + PeekByte(bank,pointer)
					fg	= ((dcol And $FF00) Shr 8) + PeekByte(bank,pointer + 1)
					fb	= (dcol And $FF) + PeekByte(bank,pointer + 2)
					
					If fr > 255 Then fr = 255
					If fg > 255 Then fg = 255
					If fb > 255 Then fb = 255
					
					dlum = fr
					
					If fg > dlum
						
						dlum = fg
						
					ElseIf fb > dlum
						
						dlum = fb
						
					EndIf
					
					If dlum >= lum
						
						WritePixelFast sx,sy,(fr Shl 16) Or (fg Shl 8) Or fb
						
					EndIf
					
				EndIf
				
			EndIf 
			
			pointer = pointer + 4
			
		Next
		
	Next
	
	UnlockBuffer BackBuffer()
	
End Function






;~IDEal Editor Parameters:
;~F#367#409#41B#425#4F3#506#520#529#53C#55C#570#59E#5B4#5F6
;~C#Blitz3D