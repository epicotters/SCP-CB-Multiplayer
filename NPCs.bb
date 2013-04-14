
Global Curr173.NPCs, Curr106.NPCs, Curr096.NPCs
Const NPCtype173% = 1, NPCtypeOldMan% = 2, NPCtypeGuard% = 3, NPCtypeD% = 4
Const NPCtype372% = 6, NPCtypeApache% = 7, NPCtypeMTF% = 8, NPCtype096 = 9
Const NPCtype049% = 10, NPCtypeZombie% = 11

Type NPCs
	Field obj%, obj2%, obj3%, obj4%, Collider%
	Field NPCtype%, ID%
	Field DropSpeed#, Gravity%
	Field State#, State2#, State3#, PrevState%
	
	Field Angle#
	Field Sound%, SoundChn%, SoundTimer#
	
	Field Speed#, CurrSpeed#
	
	Field Idle#
	
	Field Reload#
	
	Field LastSeen%, LastDist#
	
	Field PrevX#, PrevY#, PrevZ#
	
	Field Target.NPCs, TargetID%
	Field EnemyX#, EnemyY#, EnemyZ#
	
	Field Path.WayPoints[20], PathStatus%, PathTimer#, PathLocation%
End Type

Function CreateNPC.NPCs(NPCtype%, x#, y#, z#)
	Local n.NPCs = New NPCs, n2.NPCs
	Local temp#, i%, diff1, bump1, spec1
	Local bodybump, facebump, helmetbump
	Local sf, b, t1
	
	n\NPCtype = NPCtype
	Select NPCtype
		Case NPCtype173
			n\Collider = CreatePivot()
			EntityRadius n\Collider, 0.32
			EntityType n\Collider, HIT_PLAYER
			n\Gravity = True
			
			n\obj = LoadMesh("GFX\npcs\173_2.b3d")
			temp# = (GetINIFloat("DATA\NPCs.ini", "SCP-173", "scale") / MeshDepth(n\obj))			
			ScaleEntity n\obj, temp,temp,temp
			
			If BumpEnabled Then 		
				diff1 = LoadTexture("GFX\npcs\173texture.png")
				bump1 = LoadTexture("GFX\npcs\173_norm.jpg")
				spec1 = LoadTexture("GFX\npcs\173_spec.jpg")
				TextureBlend bump1, FE_BUMP
				TextureBlend spec1, FE_SPECULAR0
				
				;EntityTexture n\obj, bump1, 0, 0
				;EntityTexture n\obj, spec1, 0, 1
				;EntityTexture n\obj, diff1, 0, 2
				
				EntityTexture n\obj, spec1, 0, 0
				EntityTexture n\obj, bump1, 0, 1
				EntityTexture n\obj, diff1, 0, 2
			EndIf
			
			;SetAnimTime n\obj, 68	
			
			n\Speed = (GetINIFloat("DATA\NPCs.ini", "SCP-173", "speed") / 100.0)
			;n\obj = loadMesh("GFX\mesh\173.3ds")
			;scaleentity n\obj, 0.35 / meshWidth(n\obj), 0.30 / meshWidth(n\obj), 0.30 / meshWidth(n\obj)
			
		Case NPCtypeOldMan
			n\Collider = CreatePivot()
			EntityRadius n\Collider, 0.2
			EntityType n\Collider, HIT_PLAYER
			n\obj = LoadAnimMesh("GFX\npcs\106_2.b3d")
			;n\State = 20;0000
			
			If BumpEnabled Then 		
				diff1 = LoadTexture("GFX\npcs\106_diffuse.png")
				bump1 = LoadTexture("GFX\npcs\106_normals.png")
				TextureBlend bump1, FE_BUMP
				;TextureBlend di1, FE_SPECULAR0
				
				EntityTexture n\obj, bump1, 0, 0
				EntityTexture n\obj, diff1, 0, 1
			EndIf
			
			temp# = (GetINIFloat("DATA\NPCs.ini", "SCP-106", "scale") / 2.2)		
			ScaleEntity n\obj, temp, temp, temp
			
			Local OldManEyes% = LoadTexture("GFX\npcs\oldmaneyes.jpg")
			
			n\Speed = (GetINIFloat("DATA\NPCs.ini", "SCP-106", "speed") / 100.0)
			
			n\obj2 = CreateSprite()
			ScaleSprite(n\obj2, 0.03, 0.03)
			EntityTexture(n\obj2, OldManEyes)
			EntityBlend (n\obj2, BLEND_ADD)
			EntityFX(n\obj2, 1 + 8)
			SpriteViewMode(n\obj2, 2)
		Case NPCtypeGuard
			n\Collider = CreatePivot()
			EntityRadius n\Collider, 0.2
			EntityType n\Collider, HIT_PLAYER
			n\obj = LoadAnimMesh("GFX\npcs\mtf.b3d")
			n\Speed = (GetINIFloat("DATA\NPCs.ini", "Guard", "speed") / 100.0)
			temp# = (GetINIFloat("DATA\NPCs.ini", "Guard", "scale") / 15.64)	* 4
			
			tex = LoadTexture("GFX\npcs\body.jpg")
			
			For i = 1 To CountSurfaces(n\obj)
				sf = GetSurface(n\obj,i)
				b = GetSurfaceBrush( sf )
				t1 = GetBrushTexture(b,0)
				DebugLog StripPath(TextureName(t1))
				
				Select Lower(StripPath(TextureName(t1)))
					Case "mtfbody.jpg"
						TextureFilter(t1, 64)
						BrushTexture b, tex, 0, 0
						PaintSurface sf,b
						
						If StripPath(TextureName(t1)) <> "" Then FreeTexture t1
						FreeBrush b	
				End Select
			Next
			
			ScaleEntity n\obj, temp, temp, temp
		Case NPCtypeMTF
			n\Collider = CreatePivot()
			EntityRadius n\Collider, 0.2
			EntityType n\Collider, HIT_PLAYER
			n\obj = LoadAnimMesh("GFX\npcs\mtf.b3d")
			
			n\Speed = (GetINIFloat("DATA\NPCs.ini", "MTF", "speed") / 100.0)
			
			temp# = (GetINIFloat("DATA\NPCs.ini", "MTF", "scale") / 15.64)	* 4
			ScaleEntity n\obj, temp, temp, temp
		Case NPCtypeD
			n\Collider = CreatePivot()
			EntityRadius n\Collider, 0.32
			EntityType n\Collider, HIT_PLAYER
			
			For n2.NPCs = Each NPCs
				If n\NPCtype = n2\NPCtype And n<>n2 Then
					n\obj = CopyEntity (n2\obj)
					Exit
				EndIf
			Next
			
			If n\obj = 0 Then 
				n\obj = LoadAnimMesh("GFX\npcs\classd.b3d")
				
				temp# = 0.5 / MeshWidth(n\obj)
				ScaleEntity n\obj, temp, temp, temp
			EndIf
		Case NPCtype372
			n\Collider = CreatePivot()
			EntityRadius n\Collider, 0.2
			n\obj = LoadAnimMesh("GFX\npcs\372.b3d")
			
			temp# = 0.35 / MeshWidth(n\obj)
			ScaleEntity n\obj, temp, temp, temp
		Case NPCtype096
			n\Collider = CreatePivot()
			EntityRadius n\Collider, 0.2
			EntityType n\Collider, HIT_PLAYER
			n\obj = LoadAnimMesh("GFX\npcs\scp096.b3d")
			
			n\Speed = (GetINIFloat("DATA\NPCs.ini", "SCP-096", "speed") / 100.0)
			
			temp# = (GetINIFloat("DATA\NPCs.ini", "SCP-096", "scale") / 3.0)
			ScaleEntity n\obj, temp, temp, temp	
		Case NPCtype049
			n\Collider = CreatePivot()
			EntityRadius n\Collider, 0.2
			EntityType n\Collider, HIT_PLAYER
			n\obj = LoadAnimMesh("GFX\npcs\scp-049.b3d")
			
			n\Speed = (GetINIFloat("DATA\NPCs.ini", "SCP-049", "speed") / 100.0)
			
			temp# = GetINIFloat("DATA\NPCs.ini", "SCP-049", "scale")
			ScaleEntity n\obj, temp, temp, temp	
			
			n\Sound = LoadSound("SFX\Horror13.ogg")
		Case NPCtypeZombie
			n\Collider = CreatePivot()
			EntityRadius n\Collider, 0.2
			EntityType n\Collider, HIT_PLAYER
			n\obj = LoadAnimMesh("GFX\npcs\zombie1.b3d")
			
			n\Speed = (GetINIFloat("DATA\NPCs.ini", "SCP-049-2", "speed") / 100.0)
			
			temp# = (GetINIFloat("DATA\NPCs.ini", "SCP-049-2", "scale") / 22.0)
			ScaleEntity n\obj, temp, temp, temp	
			
			SetAnimTime(n\obj, 3.0)
			
			n\Sound = LoadSound("SFX\zombiebreath.ogg")
		Case NPCtypeApache
			n\Collider = CreatePivot()
			EntityRadius n\Collider, 0.2
			n\obj = LoadAnimMesh("GFX\apache.b3d")
			
			n\obj2 = LoadAnimMesh("GFX\apacherotor.b3d",n\obj)
			
			For i = -1 To 1 Step 2
				Local rotor2 = CopyEntity(n\obj2,n\obj2)
				RotateEntity rotor2,0,4.0*i,0
				EntityAlpha rotor2, 0.5
			Next
			
			n\obj3 = LoadAnimMesh("GFX\apacherotor2.b3d",n\obj)
			PositionEntity n\obj3, 0.0, 2.15, -5.48
			
			EntityType n\Collider, HIT_APACHE
			EntityRadius n\Collider, 3.0
			
			For i = -1 To 1 Step 2
				Local Light1 = CreateLight(2,n\obj)
				;room\LightDist[i] = range
				LightRange(Light1,2.0)
				LightColor(Light1,255,255,255)
				PositionEntity(Light1, 1.65*i, 1.17, -0.25)
				
				Local lightsprite = CreateSprite(n\obj)
				PositionEntity(lightsprite, 1.65*i, 1.17, 0, -0.25)
				ScaleSprite(lightsprite, 0.13, 0.13)
				EntityTexture(lightsprite, LightSpriteTex(0))
				EntityBlend (lightsprite, 3)
				EntityFX lightsprite, 1+8				
			Next
			
			temp# = 0.6
			ScaleEntity n\obj, temp, temp, temp
			
			
	End Select
	
	PositionEntity(n\Collider, x, y, z)
	PositionEntity(n\obj, x, y, z)
	
	ResetEntity(n\Collider)
	
	temp = 1
	For n2.NPCs = Each NPCs
		If n2\ID > 0 Then
			temp=temp+1
		EndIf
	Next
	
	n\ID = temp
	
	Return n
End Function

Function RemoveNPC(n.NPCs)
	
	If n\obj2 <> 0 Then 
		FreeEntity n\obj2
		n\obj2 = 0
	EndIf
	If n\obj3 <> 0 Then 
		FreeEntity n\obj3
		n\obj3 = 0
	EndIf
	If n\obj4 <> 0 Then 
		FreeEntity n\obj4
		n\obj4 = 0
	EndIf
	
	FreeEntity(n\obj) : n\obj = 0
	FreeEntity(n\Collider) : n\Collider = 0	
	
	Delete n
End Function


Function UpdateNPCs()
	Local n.NPCs, n2.NPCs, d.Doors, de.Decals, r.Rooms
	Local i%, dist#, dist2#, angle#, x#, y#, z#, prevframe#
	
	Local target
	
	For n.NPCs = Each NPCs
		Select n\NPCtype
			Case NPCtype173
				
				If Disabled173 Then
					HideEntity n\obj
					HideEntity n\Collider
				Else
					;[Block]
					PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.32, EntityZ(n\Collider))
					RotateEntity (n\obj, 0, EntityYaw(n\Collider)-180, 0)
					
					Local temp% = False
					dist# = EntityDistance(n\Collider, Collider)
					If dist < 8.0 Then 
						If EntityVisible(n\Collider, Collider) Then
							temp = True
							n\EnemyX = EntityX(Collider, True)
							n\EnemyY = EntityY(Collider, True)
							n\EnemyZ = EntityZ (Collider, True)
						EndIf
					ElseIf dist > 30.0 
						If Rand(50)=1 Then
							If PlayerRoom\RoomTemplate\Name <> "exit1" And PlayerRoom\RoomTemplate\Name <> "gatea" And PlayerRoom\RoomTemplate\Name <> "pocketdimension" Then
								For w.waypoints = Each WayPoints
									If w\door=Null And Rand(5)=1 Then
										x = Abs(EntityX(n\Collider)-EntityX(w\obj,True))
										If x < 28.0 And x > 22.0 Then
											z = Abs(EntityZ(n\Collider)-EntityZ(w\obj,True))
											If z < 28 And z > 22.0 Then
												DebugLog "MOVING 173 TO "+w\room\roomtemplate\name
												PositionEntity n\Collider, EntityX(w\obj,True), EntityY(w\obj,True)+0.25,EntityZ(w\obj,True)
												ResetEntity n\Collider
												Exit
											EndIf
										EndIf
									EndIf
								Next
							EndIf
						EndIf
					EndIf
					
					If n\Idle = False Then
						
						Local SoundVol# = Max(Min((Distance(EntityX(n\Collider), EntityZ(n\Collider), n\PrevX, n\PrevZ) * 2.5), 1.0), 0.0)
						n\SoundChn = LoopSound2(StoneDragSFX, n\SoundChn, Camera, n\Collider, 10.0, n\State)
						
						n\PrevX = EntityX(n\Collider)
						n\PrevZ = EntityZ(n\Collider)
						
						;player is looking at it -> doesn't move
						If EntityInView(n\obj, Camera) And (BlinkTimer > - 6 Or BlinkTimer < - 16) And dist < 15.0 Then
							
							BlurVolume = Max(Max(Min((4.0 - dist) / 6.0, 0.9), 0.1), BlurVolume)
							CurrCameraZoom = Max(CurrCameraZoom, (Sin(Float(MilliSecs())/20.0)+1.0)*15.0*Max((3.5-dist)/3.5,0.0))
							If temp Then
								If dist < 3.5 And MilliSecs() - n\LastSeen > 60000 Then PlaySound(HorrorSFX(3)) : n\LastSeen = MilliSecs()
							EndIf
							
							If dist < 1.5 And Rand(700) = 1 Then PlaySound2(Scp173SFX(Rand(0, 2)), Camera, n\obj)
							
							If dist < 1.5 And n\LastDist > 2.0 Then
								CurrCameraZoom = 40.0
								HeartBeatRate = Max(HeartBeatRate, 140)
								HeartBeatVolume = 0.5
								
								Select Rand(3)
									Case 1
										PlaySound(HorrorSFX(2))	
									Case 2
										PlaySound(HorrorSFX(9))
									Case 3
										PlaySound(HorrorSFX(10))				
								End Select
							EndIf
							n\LastDist = dist
							
							n\State = Max(0, n\State - FPSfactor / 20)
						Else 
							
							n\State = CurveValue(SoundVol, n\State, 3)
							If Rand(15) = 1 Then
								For d.Doors = Each Doors
									If (Not d\locked) And d\open = False And d\Code = "" Then
										For  i% = 0 To 1
											If d\buttons[i] <> 0 Then
												If Abs(EntityX(n\Collider) - EntityX(d\buttons[i])) < 0.5 Then
													If Abs(EntityZ(n\Collider) - EntityZ(d\buttons[i])) < 0.5 Then
														If (d\openstate >= 180 Or d\openstate <= 0) Then
															pvt = CreatePivot()
															PositionEntity pvt, EntityX(n\Collider), EntityY(n\Collider) + 0.5, EntityZ(n\Collider)
															PointEntity pvt, d\buttons[i]
															MoveEntity pvt, 0, 0, n\Speed * 0.6
															
															If EntityPick(pvt, 0.5) = d\buttons[i] Then UseDoor(d,False)
															
															FreeEntity pvt
														EndIf
													EndIf
												EndIf
											EndIf
										Next
									EndIf
								Next
							;ElseIf Rand(15)=1
							;173 raapii ovea
							;PlaySound2(AmbientSFX(4),Camera, n\obj)
							EndIf
							
							;player is not looking and he's visible from 173's position -> attack
							If temp Then 				
								If dist < 0.65 Then
									If KillTimer >= 0 And (Not GodMode) Then
										;showEntity(Blood)
										If (Not GodMode) Then n\Idle = True
										PlaySound(DamageSFX(Rand(0, 2)))
										If Rand(2) = 1 Then
											TurnEntity(Camera, 0, Rand(80,100), 0)
										Else
											TurnEntity(Camera, 0, Rand(-100,-80), 0)
										EndIf
										Kill()
										;RotateEntity(Head, 0, EntityYaw(Head) + Rand(-45, 45), 0)
									EndIf
								Else
									PointEntity(n\Collider, Collider)
									RotateEntity n\Collider, 0, EntityYaw(n\Collider), EntityRoll(n\Collider)
									MoveEntity(n\Collider, 0, 0, n\Speed * FPSfactor)
								EndIf
								
								;looks up
								;If (EntityY(Collider)-EntityY(n\Collider))>0.7 Then 
								;	SetAnimTime n\obj, 15
								;Else
								;	Select Rand(5)
								;		Case 1
								;			PointEntity(n\Collider, Collider)
								;			RotateEntity n\Collider, 0, EntityYaw(n\Collider)+20, EntityRoll(n\Collider)
								;			SetAnimTime n\obj, 30
								;		Case 2
								;			PointEntity(n\Collider, Collider)
								;			RotateEntity n\Collider, 0, EntityYaw(n\Collider)+45, EntityRoll(n\Collider)											
								;			SetAnimTime n\obj, 49
								;		Case 3
								;			PointEntity(n\Collider, Collider)
								;			RotateEntity n\Collider, 0, EntityYaw(n\Collider)-35, EntityRoll(n\Collider)												
								;			SetAnimTime n\obj, 39
								;		Case 4
								;			PointEntity(n\Collider, Collider)
								;			RotateEntity n\Collider, 0, EntityYaw(n\Collider)-90, EntityRoll(n\Collider)												
								;			SetAnimTime n\obj, 68	
								;		Case 5
								;			PointEntity(n\Collider, Collider)
								;			RotateEntity n\Collider, 0, EntityYaw(n\Collider), EntityRoll(n\Collider)
								;			SetAnimTime n\obj, 0
								;	End Select									
								;EndIf
								
							Else ;player is not visible -> move to the location where he was last seen							
								If n\EnemyX <> 0 Then						
									If Distance(EntityX(n\Collider), EntityZ(n\Collider), n\EnemyX, n\EnemyZ) > 0.2 Then
										pvt% = CreatePivot()
										PositionEntity(pvt, n\EnemyX, n\EnemyY, n\EnemyZ, True)
										PointEntity(n\Collider, pvt)
										RotateEntity n\Collider, 0, EntityYaw(n\Collider), EntityRoll(n\Collider)
										MoveEntity(n\Collider, 0, 0, n\Speed * FPSfactor)
										FreeEntity pvt
										If Rand(30) = 1 Then n\EnemyX = 0 : n\EnemyY = 0 : n\EnemyZ = 0
									Else
										n\EnemyX = 0 : n\EnemyY = 0 : n\EnemyZ = 0
									End If
								Else
									MoveEntity(n\Collider, 0, 0, n\Speed * 0.8 * FPSfactor)
									
									TurnEntity (n\Collider, 0, 8.0*FPSfactor, 10)
								End If
							EndIf
							
							
						EndIf
						
					EndIf
					
					;[End block]
					
				EndIf
				
			Case NPCtypeOldMan ;------------------------------------------------------------------------------------------------------------------
				
				If Contained106 Then
					n\Idle = True
					HideEntity n\obj
					PositionEntity n\obj, 0,500.0,0, True
				Else
					
					dist = EntityDistance(n\Collider, Collider)
					
					If (Not n\Idle) Then
						If n\State <= 0 Then	;attacking	
							If EntityY(n\Collider) < EntityY(Collider) - 20.0 - 0.55 Then
								If Not PlayerRoom\RoomTemplate\DisableDecals Then
									de.Decals = CreateDecal(0, EntityX(Collider), 0.01, EntityZ(Collider), 90, Rand(360), 0)
									de\Size = 0.05 : de\SizeChange = 0.001 : EntityAlpha(de\obj, 0.8) : UpdateDecals
								EndIf
								PositionEntity(n\Collider, EntityX(Collider), EntityY(Collider) - 15, EntityZ(Collider))
								PlaySound(HorrorSFX(5))
								PlaySound(DecaySFX(0))
							End If
							
							If Rand(500) = 1 Then PlaySound2(OldManSFX(Rand(0, 2)), Camera, n\Collider)
							n\SoundChn = LoopSound2(OldManSFX(4), n\SoundChn, Camera, n\Collider,8.0, 0.8)
							
							If n\State > - 10 Then
								ShouldPlay = 66
								If AnimTime(n\obj)<259 Then
									;TranslateEntity n\Collider, 0, (EntityY(Collider) - 0.35 - EntityY(n\Collider)) / 80.0, 0
									PositionEntity n\Collider, EntityX(n\Collider), EntityY(Collider) - 0.35, EntityZ(n\Collider)
									PointEntity n\obj, Collider
									RotateEntity (n\Collider, 0, CurveValue(EntityYaw(n\obj),EntityYaw(n\Collider),100.0), 0, True)
									;If AnimTime(n\obj)=>259 Then
									;	Animate2(n\obj, AnimTime(n\obj), 334, 494, 0.3)	
									;Else
									Animate2(n\obj, AnimTime(n\obj), 110, 259, 0.15, False)
									;EndIf
								Else
									n\State = -10
								EndIf
							Else
								If PlayerRoom\RoomTemplate\Name <> "gatea" Then ShouldPlay = 1
								
								Local Visible% = EntityVisible(n\Collider, Collider)
								
								If EntityInView(n\Collider, Camera) Then
									Achievements(Achv106) = True
									
									BlurVolume = Max(Max(Min((4.0 - dist) / 6.0, 0.9), 0.1), BlurVolume)
									CurrCameraZoom = Max(CurrCameraZoom, (Sin(Float(MilliSecs())/20.0)+1.0) * 20.0 * Max((4.0-dist)/4.0,0))
									
									If MilliSecs() - n\LastSeen > 60000 Then 
										CurrCameraZoom = 40
										PlaySound(HorrorSFX(6))
										n\LastSeen = MilliSecs()
									EndIf
								Else
									
									If (Not Visible) Then
										n\State=n\State-FPSfactor
									EndIf
								End If
								
								If dist > 0.8 Then
									If (dist > 40.0 Or n\PathStatus=2 Or (n\PathStatus=0 And n\PathTimer=>0) Or PlayerRoom\RoomTemplate\Name = "pocketdimension" Or Visible) And PlayerRoom\RoomTemplate\Name <> "gatea" Then 
										
										If dist > 40.0 Then DebugLog "noclip - dist > 40"
										If n\PathStatus = 2 Then DebugLog "noclip - n\pathstatus = 2"
										If Visible Then DebugLog "noclip - visible"
										
										TranslateEntity n\Collider, 0, ((EntityY(Collider) - 0.14) - EntityY(n\Collider)) / 50.0, 0
										
										PointEntity n\obj, Collider
										;rotateentity(n\collider, entityPitch(n\collider),))
										RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 10.0), 0
										
										If KillTimer >= 0 Then
											n\CurrSpeed = CurveValue(n\Speed,n\CurrSpeed,10.0)
											Local atemp# = AnimTime(n\obj)
											Animate2(n\obj, AnimTime(n\obj), 284, 333, n\CurrSpeed*35)
											
											If atemp =< 286 And AnimTime(n\obj)>286 Then
												PlaySound2(StepPDSFX(Rand(0,2)),Camera, n\Collider, 6.0, Rnd(0.8,1.0))	
											ElseIf atemp=<311 And AnimTime(n\obj)>311.0 
												PlaySound2(StepPDSFX(Rand(0,2)),Camera, n\Collider, 6.0, Rnd(0.8,1.0))
											EndIf
										Else 
											n\CurrSpeed = 0
										EndIf
										
										n\PathTimer = Max(n\PathTimer-FPSfactor,0)
										If n\PathTimer =< 0 Then n\PathStatus = 0
									Else 
										
										DebugLog "pathfinding"
										
										If n\PathTimer <= 0 Then
											n\PathStatus = FindPath (n, EntityX(Collider,True), EntityY(Collider,True), EntityZ(Collider,True))
											n\PathTimer = 70*10
											n\CurrSpeed = 0
										Else
											n\PathTimer = Max(n\PathTimer-FPSfactor,0)
											
											If n\PathStatus = 2 Then
												DebugLog "no path found"
												n\CurrSpeed = 0
											ElseIf n\PathStatus = 1
												DebugLog "path found"
												If n\Path[n\PathLocation]=Null Then 
													If n\PathLocation > 19 Then 
														n\PathLocation = 0 : n\PathStatus = 0
													Else
														n\PathLocation = n\PathLocation + 1
													EndIf
												Else
													TranslateEntity n\Collider, 0, ((EntityY(n\Path[n\PathLocation]\obj,True) - 0.15) - EntityY(n\Collider)) / 50.0, 0
													
													PointEntity n\obj, n\Path[n\PathLocation]\obj
													
													RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 10.0), 0
													n\CurrSpeed = CurveValue(n\Speed,n\CurrSpeed,10.0)
													
													atemp# = AnimTime(n\obj)
													Animate2(n\obj, AnimTime(n\obj), 284, 333, n\CurrSpeed*35)
													If atemp > 13 And AnimTime(n\obj)<1.0 Then
														;PlaySound2(StepPDSFX(Rand(0,2)),Camera, n\Collider, 6.0, Rnd(0.8,1.0))	
													ElseIf atemp=< 7 And AnimTime(n\obj)>7.0 
														;PlaySound2(StepPDSFX(Rand(0,2)),Camera, n\Collider, 6.0, Rnd(0.8,1.0))
													EndIf
													
													If EntityDistance(n\Collider,n\Path[n\PathLocation]\obj) < 0.2 Then
														n\PathLocation = n\PathLocation + 1
													EndIf 
												EndIf
												;rotateentity(n\collider, entityPitch(n\collider),))
											ElseIf n\PathStatus = 0
												DebugLog "pathstatus = 0"
												If n\State3=0 Then Animate2(n\obj, AnimTime(n\obj), 334, 494, 0.3)
												n\CurrSpeed = CurveValue(0,n\CurrSpeed,10.0)
											EndIf
										EndIf
										
									EndIf
									
								ElseIf PlayerRoom\RoomTemplate\Name <> "gatea" ;dist < 0.8
									
									If dist > 0.5 Then 
										n\CurrSpeed = CurveValue(n\Speed * 2.5,n\CurrSpeed,10.0)
									Else
										n\CurrSpeed = 0
									EndIf
									Animate2(n\obj, AnimTime(n\obj), 105, 110, 0.15, False)
									;If Floor(AnimTime(n\obj)) = 43 Then SetAnimTime(n\obj, 43)
									
									If KillTimer >= 0 And FallTimer >= 0 Then
										PointEntity n\obj, Collider
										RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 10.0), 0										
										
										If Ceil(AnimTime(n\obj)) = 110 Then
											If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then
												PlaySound(DeathSFX(Rand(1,3)))
												Kill()
											Else
												PlaySound(DeathSFX(1))
												PlaySound(OldManSFX(3))
												FallTimer = Min(-1, FallTimer)
												PositionEntity(Head, EntityX(Camera, True), EntityY(Camera, True), EntityZ(Camera, True), True)
												ResetEntity (Head)
												RotateEntity(Head, 0, EntityYaw(Camera) + Rand(-45, 45), 0)
											EndIf
										EndIf
									EndIf
									
								EndIf
								
							EndIf 
							
							MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
							
							n\DropSpeed = 0
							If n\State <= Rand(-2800, -2200) Then 
								If Not EntityInView(n\obj,Camera) Then n\State = Rand(22000, 27000)
							EndIf
							
							If FallTimer < -250.0 Then
								For r.Rooms = Each Rooms
									If r\RoomTemplate\Name = "pocketdimension" Then
										FallTimer = 0
										UpdateDoors()
										UpdateRooms()
										ShowEntity Collider
										PlaySound(Use914SFX)
										PlaySound(OldManSFX(5))
										PositionEntity(Collider, EntityX(r\obj),0.8,EntityZ(r\obj))
										DropSpeed = 0
										ResetEntity Collider
										n\State = -2500
										BlinkTimer = -10
										Injuries = Injuries+0.5
										
										Exit
									EndIf
								Next												
							EndIf
							
						Else ;idling outside the map
							n\CurrSpeed = 0
							MoveEntity n\Collider, 0, ((EntityY(Collider) - 30) - EntityY(n\Collider)) / 200.0, 0
							n\DropSpeed = 0
							SetAnimTime n\obj, 110.0
							
							If (Not PlayerRoom\RoomTemplate\DisableDecals) Then n\State=n\State-FPSfactor
						End If
					EndIf
					
					ResetEntity(n\Collider)
					PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.15, EntityZ(n\Collider))
					
					RotateEntity n\obj, 0, EntityYaw(n\Collider), 0
					
					PositionEntity(n\obj2, EntityX(n\obj), EntityY(n\obj) , EntityZ(n\obj))
					RotateEntity(n\obj2, 0, EntityYaw(n\Collider) - 180, 0)
					MoveEntity(n\obj2, 0, 9.14 * 0.098, -1.5 * 0.098)
					
					If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then
						EntityAlpha n\obj2, 0.0
					Else
						If dist > 5.5 Then
							EntityAlpha(n\obj2, 1.0)
						ElseIf dist > 4.5
							EntityAlpha(n\obj2, Min(dist - 4.5, 1.0))
						Else
							EntityAlpha(n\obj2, 0)
						End If
					EndIf
					
				EndIf
			Case NPCtype096
				
				dist = EntityDistance(Collider, n\Collider)
				
				Select n\State
					Case 0
						If dist<8.0 Then
							Achievements(Achv096) = True
							If n\Sound = 0 Then
								n\Sound = LoadSound("SFX\096_1.ogg")
							Else
								n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider, 8.0, 1.0)
							EndIf
							
							Animate2(n\obj, AnimTime(n\obj),364,425, 0.20)
							pvt = CreatePivot()
							PositionEntity pvt, EntityX(n\Collider),EntityY(n\Collider),EntityZ(n\Collider),True
							PointEntity pvt, Camera
							
							angle = WrapAngle(EntityYaw(pvt)-EntityYaw(n\Collider))
							If angle<55 Or angle>360-55 Then
								If EntityInView(n\Collider, Camera) Then 
									If EntityVisible(Collider, n\Collider) Then
										SetAnimTime n\obj, 54
										StopChannel n\SoundChn
										FreeSound n\Sound
										n\Sound = 0
										n\State = 1
									EndIf
								EndIf
							EndIf
							
							FreeEntity pvt
						EndIf
					Case 4
						CurrCameraZoom = CurveValue(Max(CurrCameraZoom, (Sin(Float(MilliSecs())/20.0)+1.0) * 10.0),CurrCameraZoom,8.0)
						If n\Sound = 0 Then
							n\Sound = LoadSound("SFX\096_3.ogg")
						Else
							n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider, 7.5, 1.0)
						EndIf
						
						If MilliSecs() > n\State3 Then
							n\LastSeen=0
							If n\Target=Null Then
								If EntityVisible(Collider, n\Collider) Then n\LastSeen=1
							Else
								If EntityVisible(n\Target\Collider, n\Collider) Then n\LastSeen=1
								
							EndIf
							n\State3=MilliSecs()+3000
						EndIf
						
						If n\LastSeen=1 And KillTimer => 0 Then
							n\PathTimer=Max(70*3, n\PathTimer)
							n\PathStatus=0
							
							If n\Target<> Null Then dist = EntityDistance(n\Target\Collider, n\Collider)
							
							If dist<0.6 Then
								n\CurrSpeed = CurveValue(0,n\CurrSpeed,5.0)
								n\State2 = n\State2+FPSfactor
								
								If n\Target=Null Then
									PointEntity n\Collider, Collider
									RotateEntity n\Collider, 0, EntityYaw(n\Collider)+Sin(n\State2*3.5)*30, 0
									
									If (Not GodMode) Then 
										PlaySound DeathSFX(4)
										
										pvt = CreatePivot()
										CameraShake = 30
										BlurTimer = 2000
										Kill()
										TurnEntity Camera, 0, -90, 0
										For i = 0 To 6
											PositionEntity pvt, EntityX(Collider)+Rnd(-0.1,0.1),EntityY(Collider)-0.05,EntityZ(Collider)+Rnd(-0.1,0.1)
											TurnEntity pvt, 90, 0, 0
											EntityPick(pvt,0.3)
											
											de.Decals = CreateDecal(Rand(15,16), PickedX(), PickedY()+0.005, PickedZ(), 90, Rand(360), 0)
											de\Size = Rnd(0.2,0.6) : EntityAlpha(de\obj, 1.0) : ScaleSprite de\obj, de\Size, de\Size
										Next
										FreeEntity pvt
									EndIf									
								Else
									PointEntity n\Collider, n\Target\Collider
									RotateEntity n\Collider, 0, EntityYaw(n\Collider)+Sin(n\State2*3.5)*30, 0
								EndIf
								
								Animate2(n\obj, AnimTime(n\obj),37,27, -0.3, False)	
							Else
								If n\Target=Null Then 
									DebugLog "target=null"
									PointEntity n\obj, Collider
								Else
									PointEntity n\obj, n\Target\Collider
								EndIf
								
								RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 5.0), 0
								n\CurrSpeed = CurveValue(n\Speed,n\CurrSpeed,20.0)
								
								If dist < 2.0 Then 
									If AnimTime(n\obj)>54 Then SetAnimTime(n\obj, 28)
									If AnimTime(n\obj)<27 Then
										Animate2(n\obj, AnimTime(n\obj),2,27, n\CurrSpeed*10)	
									Else
										Animate2(n\obj, AnimTime(n\obj),28,54, n\CurrSpeed*10)	
									EndIf
								Else
									Animate2(n\obj, AnimTime(n\obj),338,363, n\CurrSpeed*10)	
								EndIf								
							EndIf
							
							MoveEntity n\Collider, 0,0,n\CurrSpeed
							
							If KillTimer < 0 Then
								n\State=2
								n\State2=0
							EndIf
						Else
							If n\PathStatus = 1 Then
								
								If n\Path[n\PathLocation]=Null Then 
									If n\PathLocation > 19 Then 
										n\PathLocation = 0 : n\PathStatus = 0
									Else
										n\PathLocation = n\PathLocation + 1
									EndIf
								Else
									PointEntity n\obj, n\Path[n\PathLocation]\obj
									
									RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 5.0), 0
									n\CurrSpeed = CurveValue(n\Speed,n\CurrSpeed,20.0)
									
									MoveEntity n\Collider, 0,0,n\CurrSpeed
									
									Animate2(n\obj, AnimTime(n\obj),338,363, n\CurrSpeed*10)
									
									;atemp# = AnimTime(n\obj)
									;Animate2(n\obj, AnimTime(n\obj), 284, 333, n\currspeed*35)
									;If atemp > 13 And AnimTime(n\obj)<1.0 Then
													;PlaySound2(StepPDSFX(Rand(0,2)),Camera, n\Collider, 6.0, Rnd(0.8,1.0))	
									;ElseIf atemp=< 7 And AnimTime(n\obj)>7.0 
													;PlaySound2(StepPDSFX(Rand(0,2)),Camera, n\Collider, 6.0, Rnd(0.8,1.0))
									;EndIf
									dist2# = EntityDistance(n\Collider,n\Path[n\PathLocation]\obj)
									If dist2 < 0.6 Then
										If n\Path[n\PathLocation]\door <> Null Then
											If n\Path[n\PathLocation]\door\open = False Then
												n\Path[n\PathLocation]\door\open = True
												n\Path[n\PathLocation]\door\fastopen = 1
												PlaySound2(OpenDoorFastSFX, Camera, n\Path[n\PathLocation]\door\obj)
											EndIf
										EndIf							
										If dist2 < 0.2 Then n\PathLocation = n\PathLocation + 1
									EndIf 
								EndIf
								
							Else
								Animate2(n\obj, AnimTime(n\obj),217,255, 0.1)
								
								n\PathTimer = Max(0, n\PathTimer-FPSfactor)
								If n\PathTimer=<0 Then
									If n\Target<>Null Then
										n\PathStatus = FindPath(n, EntityX(n\Target\Collider),EntityY(n\Target\Collider)+0.2,EntityZ(n\Target\Collider))	
									Else
										n\PathStatus = FindPath(n, EntityX(Collider),EntityY(Collider)+0.2,EntityZ(Collider))	
									EndIf
									n\PathTimer = 70*5
								EndIf
							EndIf
						EndIf
						
						
						If dist > 24.0 
							If Rand(50)=1 Then
								For w.waypoints = Each WayPoints
									If w\door=Null And Rand(5)=1 Then
										x = Abs(EntityX(n\Collider)-EntityX(w\obj,True))
										If x < 28.0 And x > 20.0 Then
											z = Abs(EntityZ(n\Collider)-EntityZ(w\obj,True))
											If z < 28 And z > 20.0 Then
												DebugLog "TELEPORTING 096 - "+w\room\roomtemplate\name
												n\PathStatus = 0 : n\PathTimer = 0 : n\PathLocation = 0
												PositionEntity n\Collider, EntityX(w\obj,True), EntityY(w\obj,True)+0.25,EntityZ(w\obj,True)
												ResetEntity n\Collider
												Exit
											EndIf
										EndIf
									EndIf
								Next
							EndIf
						EndIf
						
					Case 1,2,3
						If n\Sound = 0 Then
							n\Sound = LoadSound("SFX\096_2.ogg")
						Else
							n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider, 10.0, 1.0)
						EndIf
						
						If n\State=1 Then
							Animate2(n\obj, AnimTime(n\obj),54,166, 0.2, False)
							If AnimTime(n\obj)>165 Then n\State = 2
						ElseIf n\State=2
							Animate2(n\obj, AnimTime(n\obj),282,337, 0.2, False)
							If AnimTime(n\obj)>426 Then ;walk to idle
								Animate2(n\obj, AnimTime(n\obj),426,523, 0.2, False)
								If AnimTime(n\obj)=>523 Then SetAnimTime(n\obj,282)
							Else
								If AnimTime(n\obj)>336 Then n\State = 3 : n\State2=0
							EndIf
							
						ElseIf n\State=3
							n\State2 = n\State2+FPSfactor
							If n\State2 > 70*18 Then 
								Animate2(n\obj, AnimTime(n\obj),217,282, 0.5, False)
								If AnimTime(n\obj)>281 Then 
									n\State = 4
									StopChannel n\SoundChn
									FreeSound n\Sound : n\Sound = 0
								EndIf
							Else
								Animate2(n\obj, AnimTime(n\obj),217,255, 0.25)
							EndIf
							RotateEntity(n\Collider, Sin(n\State2*3)*10, EntityYaw(n\Collider)+Sin(n\State2^1.2)*8, 0, True)
						EndIf
					Case 5
						If dist < 8.0 Then 
							
							n\State2=n\State2+FPSfactor
							If n\State2>1000 Then ;walking around
								If n\State2>1600 Then n\State2=Rand(0,500)
								
								If AnimTime(n\obj)<216 Then 
									SetAnimTime(n\obj,524)
								ElseIf AnimTime(n\obj)=>524 ;idle to walk
									n\CurrSpeed = CurveValue(n\Speed*0.1,n\CurrSpeed,5.0)
									Animate2(n\obj, AnimTime(n\obj),524,546, n\CurrSpeed*45,False)
									If AnimTime(n\obj)=>546 Then SetAnimTime(n\obj,426)
								Else
									Animate2(n\obj, AnimTime(n\obj),426,497, n\CurrSpeed*45)	
									n\CurrSpeed = CurveValue(n\Speed*0.1,n\CurrSpeed,5.0)
								EndIf
								
								
								If MilliSecs() > n\State3 Then
									n\LastSeen=0
									If EntityVisible(Collider, n\Collider) Then 
										n\LastSeen=1
									Else
										HideEntity n\Collider
										EntityPick(n\Collider, 1.5)
										If PickedEntity() <> 0 Then
											n\Angle = EntityYaw(n\Collider)+Rnd(80,110)
										EndIf
										ShowEntity n\Collider
									EndIf
									n\State3=MilliSecs()+3000
								EndIf
								
								If n\LastSeen Then 
									PointEntity n\obj, Collider
									RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),130.0),0
									If dist < 1.5 Then n\State2=0
								Else
									RotateEntity n\Collider, 0, CurveAngle(n\Angle,EntityYaw(n\Collider),130.0),0
								EndIf
							Else
								If AnimTime(n\obj)>216 Then ;walk to idle
									Animate2(n\obj, AnimTime(n\obj),498,523, n\CurrSpeed*42,False)
									If AnimTime(n\obj)=> 523 Then SetAnimTime(n\obj, 166)
									n\CurrSpeed = CurveValue(n\Speed*0.05,n\CurrSpeed,8.0)	
								Else ;idle
									Animate2(n\obj, AnimTime(n\obj),166,216, 0.2)
									n\CurrSpeed = CurveValue(0,n\CurrSpeed,4.0)						
								EndIf
							EndIf
							
							pvt = CreatePivot()
							PositionEntity pvt, EntityX(n\Collider),EntityY(n\Collider),EntityZ(n\Collider),True
							PointEntity pvt, Camera
							
							angle = WrapAngle(EntityYaw(pvt)-EntityYaw(n\Collider))
							If angle<55 Or angle>360-55 Then
								If EntityInView(n\Collider, Camera) Then 
									If EntityVisible(Collider, n\Collider) Then
										If n\State2<1000 Then SetAnimTime n\obj, 282
										StopChannel n\SoundChn
										FreeSound n\Sound
										n\Sound = 0
										n\State = 2
									EndIf
								EndIf
							EndIf
							
							FreeEntity pvt
							
							MoveEntity n\Collider, 0,0,n\CurrSpeed
						EndIf
				End Select
				
				;ResetEntity(n\Collider)
				PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider)+0.05, EntityZ(n\Collider))
				
				RotateEntity n\obj, EntityPitch(n\Collider)-90, EntityYaw(n\Collider), 0
				
			Case NPCtype049
				
				;n\state = the "main state" of the NPC
				;n\state2 = attacks the player when the value is above 0.0
				;n\state3 = a timer used for checking whether the player is visible every three seconds
				
				prevframe# = AnimTime(n\obj)
				
				DebugLog "state: "+n\State+"    state2: "+n\State2+"     state3:"+n\State3
				dist  = EntityDistance(Collider, n\Collider)
				
				If Abs(EntityY(Collider)-EntityY(n\Collider))<4.0 Then 
					Select n\State
						Case 0
						Case 2 ;following the player
							n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider, 10.0, 1.0)
							
							If n\State3 < 0 Then ;check if the player is visible every three seconds
								If dist < 5.0 Then 
									If EntityVisible(Collider, n\Collider) Then n\State2 = 70*5
								EndIf
								n\State3=70*3
							Else
								n\State3=n\State3-FPSfactor
							EndIf						
							
							If n\State2 > 0 Then ;player is visible -> attack
								dist = EntityDistance(Collider, n\Collider)
								
								PointEntity n\obj, Collider
								RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 15.0), 0
								
								If dist < 0.5 Then
									If Wearing714 Then
										BlurTimer = BlurTimer+FPSfactor*2.5
										If BlurTimer>250 And BlurTimer-FPSfactor*2.5 <= 250 And n\PrevState=0 Then
											TempSound = LoadSound("SFX\049\049_8.ogg")
											n\SoundChn = PlaySound(TempSound)
											n\PrevState=1
										ElseIf BlurTimer => 500
											Wearing714=False
										EndIf
									Else
										CurrCameraZoom = 20.0
										BlurTimer = 500.0
										;Kill()
										;KillAnim = 0
										If (Not GodMode) Then 
											For e.events = Each Events
												If e\room = PlayerRoom Then e\eventstate=-1
											Next
											If TempSound<>0 Then FreeSound TempSound
											TempSound = LoadSound("SFX\Horror14.ogg")
											PlaySound TempSound										
											n\State = 3
										EndIf										
									EndIf
									
								Else
									n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 20.0)
									MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor								
									
									If dist < 3.0 Then
										Animate2(n\obj, AnimTime(n\obj), Min(AnimTime(n\obj),428.0), 463.0, n\CurrSpeed*38)
									Else
										If AnimTime(n\obj)>428.0 Then
											Animate2(n\obj, AnimTime(n\obj), Min(AnimTime(n\obj),463.0), 498.0, n\CurrSpeed*38,False)
											If AnimTime(n\obj)>497.0 Then SetAnimTime n\obj, 358.0
										Else
											Animate2(n\obj, AnimTime(n\obj), Min(AnimTime(n\obj),358.0), 393.0, n\CurrSpeed*38)
										EndIf
										
									EndIf
								EndIf
								
								n\State2=n\State2-FPSfactor
							Else
								n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider, 6.0, 0.6)
								
								If n\PathStatus = 1 Then ;path found
									If n\Path[n\PathLocation]=Null Then 
										If n\PathLocation > 19 Then 
											n\PathLocation = 0 : n\PathStatus = 0
										Else
											n\PathLocation = n\PathLocation + 1
										EndIf
									Else
										PointEntity n\obj, n\Path[n\PathLocation]\obj
										
										RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 15.0), 0
										n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 20.0)
										MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
										Animate2(n\obj, AnimTime(n\obj), Min(AnimTime(n\obj),358.0), 393.0, n\CurrSpeed*38)
										
										
										;closes doors behind him
										If n\PathLocation>0 Then
											If n\Path[n\PathLocation-1]\door <> Null Then
												If n\Path[n\PathLocation-1]\door\open Then UseDoor(n\Path[n\PathLocation-1]\door, False)
											EndIf
										EndIf
										
										;opens doors in front of him
										dist2# = EntityDistance(n\Collider,n\Path[n\PathLocation]\obj)
										If dist2 < 0.6 Then
											If n\Path[n\PathLocation]\door <> Null Then
												If n\Path[n\PathLocation]\door\open = False Then UseDoor(n\Path[n\PathLocation]\door, False)
											EndIf							
											If dist2 < 0.2 Then n\PathLocation = n\PathLocation + 1
										EndIf									
										
									EndIf
									
								Else ;no path to the player, stands still
									n\CurrSpeed = 0
									Animate2(n\obj, AnimTime(n\obj), 269, 345, 0.2)
									
									n\PathTimer = n\PathTimer-FPSfactor
									If n\PathTimer =< 0 Then
										n\PathStatus = FindPath(n, EntityX(Collider),EntityY(Collider)+0.1,EntityZ(Collider))
										n\PathTimer = n\PathTimer+70*5
									EndIf
								EndIf
							EndIf
							
							If n\CurrSpeed > 0.005 Then
								If (prevframe < 361 And AnimTime(n\obj)=>361) Or (prevframe < 377 And AnimTime(n\obj)=>377) Then
									PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.2,0.3))						
								ElseIf (prevframe < 431 And AnimTime(n\obj)=>431) Or (prevframe < 447 And AnimTime(n\obj)=>447)
									PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))
								EndIf
							EndIf						
						Case 3
							Animate2(n\obj, AnimTime(n\obj), 537, 660, 0.7, False)
							PositionEntity n\Collider, CurveValue(EntityX(Collider),EntityX(n\Collider),20.0),EntityY(n\Collider),CurveValue(EntityZ(Collider),EntityZ(n\Collider),20.0)
							RotateEntity n\Collider, 0, CurveAngle(EntityYaw(Collider)-180.0,EntityYaw(n\Collider),40), 0
					End Select
					
					PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider)-0.22, EntityZ(n\Collider))
					
					RotateEntity n\obj, EntityPitch(n\Collider), EntityYaw(n\Collider), 0
					
				EndIf
			
			Case NPCtypeZombie
				prevframe# = AnimTime(n\obj)
				
				DebugLog "state: "+n\State+"    state2: "+n\State2+"     state3:"+n\State3
				
				Select n\State
					Case 0
						
					Case 1 ;stands up
						Animate2(n\obj, AnimTime(n\obj), 3.0, 30.0, 0.1, False)
						If AnimTime(n\obj)=>29.5 Then n\State = 2
					Case 2 ;following the player
						If n\State3 < 0 Then ;check if the player is visible every three seconds
							If EntityDistance(Collider, n\Collider)<5.0 Then 
								If EntityVisible(Collider, n\Collider) Then n\State2 = 70*5
							EndIf
							n\State3=70*3
						Else
							n\State3=n\State3-FPSfactor
						EndIf						
						
						If n\State2 > 0 Then ;player is visible -> attack
							dist = EntityDistance(Collider, n\Collider)
							
							PointEntity n\obj, Collider
							RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 30.0), 0
							
							If dist < 0.7 Then 
								n\State = 3
								SetAnimTime n\obj, 127.0
							Else
								n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 20.0)
								MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor								
								
								If dist < 2.0 Then
									Animate2(n\obj, AnimTime(n\obj), Min(AnimTime(n\obj),95.0), 124.0, n\CurrSpeed*30)
								Else
									Animate2(n\obj, AnimTime(n\obj), 64.0, 93.0, n\CurrSpeed*30)
								EndIf
							EndIf
							
							n\State2=n\State2-FPSfactor
						Else
							n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider, 6.0, 0.6)
							
							If n\PathStatus = 1 Then ;path found
								If n\Path[n\PathLocation]=Null Then 
									If n\PathLocation > 19 Then 
										n\PathLocation = 0 : n\PathStatus = 0
									Else
										n\PathLocation = n\PathLocation + 1
									EndIf
								Else
									PointEntity n\obj, n\Path[n\PathLocation]\obj
									
									RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 30.0), 0
									n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 20.0)
									MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
									Animate2(n\obj, AnimTime(n\obj), 64.0, 93.0, n\CurrSpeed*30)
									
									If EntityDistance(n\Collider,n\Path[n\PathLocation]\obj) < 0.2 Then
										n\PathLocation = n\PathLocation + 1
									EndIf 
								EndIf
							Else ;no path to the player, stands still
								n\CurrSpeed = 0
								Animate2(n\obj, AnimTime(n\obj), 204, 225, 0.1)
								
								n\PathTimer = n\PathTimer-FPSfactor
								If n\PathTimer =< 0 Then
									n\PathStatus = FindPath(n, EntityX(Collider),EntityY(Collider)+0.1,EntityZ(Collider))
									n\PathTimer = n\PathTimer+70*5
								EndIf
							EndIf
						EndIf
						
						;65, 80, 93, 109, 123
						If n\CurrSpeed > 0.005 Then
							If (prevframe < 65 And AnimTime(n\obj)=>65) Or (prevframe < 80 And AnimTime(n\obj)=>80) Then
								PlaySound2(StepSFX(0,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))						
							ElseIf (prevframe < 93 And AnimTime(n\obj)=>93) Or (prevframe < 109 And AnimTime(n\obj)=>109)
								PlaySound2(StepSFX(0,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))
							ElseIf (prevframe < 123 And AnimTime(n\obj)=>123)
								PlaySound2(StepSFX(0,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))	
							EndIf
						EndIf						
					Case 3
						Animate2(n\obj, AnimTime(n\obj), 127, 165, 0.7, False)
						If prevframe < 146 And AnimTime(n\obj)=>146 Then 
							PlaySound DeathSFX(Rand(1,3))
							Injuries = Injuries+Rnd(0.4,1.0)
						ElseIf AnimTime(n\obj)>164.5 Then
							n\State = 2
						EndIf
				End Select
				
				PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.22, EntityZ(n\Collider))
				
				RotateEntity n\obj, 0, EntityYaw(n\Collider)-180.0, 0
			Case NPCtypeGuard ;------------------------------------------------------------------------------------------------------------------
				prevframe# = AnimTime(n\obj)
				
				Select n\State
					Case 1 ;aims and shoots at the player
						Animate2(n\obj, AnimTime(n\obj), 110, 120, 0.2, False)
						
						If KillTimer => 0 Then
							dist = EntityDistance(n\Collider,Collider)
							
							If dist<11.0 Then
								pvt% = CreatePivot()
								PositionEntity(pvt, EntityX(n\Collider), EntityY(n\Collider) + 0.8, EntityZ(n\Collider))
								PointEntity(pvt, Collider)
								RotateEntity(pvt, Min(EntityPitch(pvt), 40), EntityYaw(pvt), 0)
								
								RotateEntity(n\Collider, CurveAngle(EntityPitch(pvt) - 10, EntityPitch(n\Collider), 10), CurveAngle(EntityYaw(pvt) + 5, EntityYaw(n\Collider), 10), 0, True)
								RotateEntity(pvt, EntityPitch(n\Collider) + 10, EntityYaw(n\Collider) - 5, 0, True)
								
								If n\Reload = 0 Then 
									EntityPick(pvt, 15.0)
									If PickedEntity() = Collider Then
										
										PlaySound2(GunshotSFX, Camera, n\Collider, 35)
										
										RotateEntity(pvt, EntityPitch(n\Collider), EntityYaw(n\Collider), 0, True)
										;p.Particles = CreateParticle(EntityX(n\obj, True), EntityY(n\obj, True), EntityZ(n\obj, True), 1, 0.2, 0.0, 5)
										PositionEntity(pvt, EntityX(n\obj), EntityY(n\obj), EntityZ(n\obj))
										MoveEntity (pvt,0.8*0.079, 10.75*0.079, 6.9*0.079)
										
										PointEntity pvt, Collider
										Shoot(EntityX(pvt),EntityY(pvt),EntityZ(pvt),EntityPitch(pvt)-Rnd(-1,1),EntityYaw(pvt)-Rnd(-1,1),dist+0.5, False)
										n\Reload = 10										
										
									Else
										n\CurrSpeed = n\Speed
									End If
								EndIf
								
								FreeEntity(pvt)									
							EndIf
							
							If dist > 6.0 Then
								angle# = EntityPitch(n\Collider)
								RotateEntity(n\Collider, 20, EntityYaw(n\Collider),0,True)
								MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
								
								RotateEntity(n\Collider, angle, EntityYaw(n\Collider),0,True)
								;PointEntity(n\collider, Camera)
							EndIf
							n\CurrSpeed = CurveValue(0, n\CurrSpeed, 100.0)
						Else
							n\State = 0
						EndIf
					Case 2 ;shoots
						Animate2(n\obj, AnimTime(n\obj), 110, 120, 0.2, False)
						If n\Reload = 0 Then 
							PlaySound2(GunshotSFX, Camera, n\Collider, 20)
							p.Particles = CreateParticle(EntityX(n\obj, True), EntityY(n\obj, True), EntityZ(n\obj, True), 1, 0.2, 0.0, 5)
							PositionEntity(p\pvt, EntityX(n\obj), EntityY(n\obj), EntityZ(n\obj))
							RotateEntity(p\pvt, EntityPitch(n\Collider), EntityYaw(n\Collider), 0, True)
							MoveEntity (p\pvt,0.8*0.079, 10.75*0.079, 6.9*0.079)
							n\Reload = 5
						End If
					Case 3 ;follows a path
						If n\PathStatus = 2 Then
							n\State = 0
							n\CurrSpeed = 0
						ElseIf n\PathStatus = 1
							If n\Path[n\PathLocation]=Null Then 
								If n\PathLocation > 19 Then 
									n\PathLocation = 0 : n\PathStatus = 0
								Else
									n\PathLocation = n\PathLocation + 1
								EndIf
							Else
								PointEntity n\obj, n\Path[n\PathLocation]\obj
								
								RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 10.0), 0
								n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 20.0)
								MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
								Animate2(n\obj, AnimTime(n\obj), 121, 150, n\CurrSpeed*26)
								
								If EntityDistance(n\Collider,n\Path[n\PathLocation]\obj) < 0.2 Then
									n\PathLocation = n\PathLocation + 1
								EndIf 
							EndIf
						Else
							n\CurrSpeed = 0
							n\State = 4
						EndIf
					Case 4
						Animate2(n\obj, AnimTime(n\obj), 165, 256, 0.5)
						
						If Rand(400) = 1 Then n\PrevState = Rnd(-180, 180)
						
						RotateEntity(n\Collider, Sin(MilliSecs() / 25) * 1.5, CurveAngle(n\Angle + n\PrevState + Sin(MilliSecs() / 50) * 2, EntityYaw(n\Collider), 150.0), 0, True)
						
						dist# = EntityDistance(n\Collider, Collider)
						If dist < 15.0 Then
							pvt = CreatePivot()
							PositionEntity(pvt, EntityX(n\Collider), EntityY(n\Collider) + 0.8, EntityZ(n\Collider))
							PointEntity(pvt, Collider)
							If WrapAngle(EntityYaw(n\Collider)-EntityYaw(pvt))<90 Then
								If EntityVisible(pvt,Collider) Then n\State = 1
							EndIf
							
							FreeEntity pvt
						EndIf
					Case 7
						
						If AnimTime(n\obj)>120 Then 
							Animate2(n\obj, AnimTime(n\obj), 121, 156, 0.1, False)
							If AnimTime(n\obj)=> 156 Then SetAnimTime(n\obj, 32)
						Else
							Animate2(n\obj, AnimTime(n\obj), 32, 81, 0.25)
						EndIf
						
					Case 8
					Case 5 ;following a target
						pvt = CreatePivot()
						PositionEntity pvt, n\EnemyX, n\EnemyY, n\EnemyZ
						
						;n\Reload = 200
						PointEntity n\obj, pvt
						RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj)+n\Angle, EntityYaw(n\Collider), 10.0), 0
						
						dist# = EntityDistance(n\obj,pvt)
						
						n\CurrSpeed = CurveValue(n\Speed*Min(1.0,(dist-1.0)*3)*1.4, n\CurrSpeed, 5.0)
						MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
						
						If Abs(n\CurrSpeed)<0.01 Then  
							Animate2(n\obj, AnimTime(n\obj), 165, 256, 0.5)
						ElseIf n\CurrSpeed > 0
							Animate2(n\obj, AnimTime(n\obj), 121, 150, n\CurrSpeed*26)
						Else
							Animate2(n\obj, AnimTime(n\obj), 150, 121, n\CurrSpeed*26)
						EndIf
						
						;n\angle = CurveValue(0,n\angle,30.0)
					Default
						If Rand(400) = 1 Then n\PrevState = Rnd(-30, 30)
						
						Animate2(n\obj, AnimTime(n\obj), 165, 256, 0.5)
						
						RotateEntity(n\Collider, Sin(MilliSecs() / 25) * 1.5, CurveAngle(n\Angle + n\PrevState + Sin(MilliSecs() / 50) * 2, EntityYaw(n\Collider), 50), 0, True)
				End Select
				
				
				If n\CurrSpeed > 0.01 Then
					If prevframe < 137 And AnimTime(n\obj)=>137 Then
						PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))						
					ElseIf prevframe < 122 And AnimTime(n\obj)=>122
						PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))
					EndIf
				EndIf
				
				n\Reload = Max(0, n\Reload - FPSfactor)
				
				PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.22, EntityZ(n\Collider))
				
				RotateEntity n\obj, EntityPitch(n\Collider), EntityYaw(n\Collider), 0
			Case NPCtypeMTF ;------------------------------------------------------------------------------------------------------------------
				dist# = EntityDistance(Collider, n\Collider)
				
				;n\state = the "main state" of the NPC
				;n\state2 = a timer for the visibility checks between the npc and the player
				
				pvt = CreatePivot()
				
				prevframe# = AnimTime(n\obj)
				
				;DebugLog n\ID +"  -  n\State = "+n\State+", n\LastSeen = "+n\LastSeen	+", n\pathstatus: "+n\PathStatus+", n\pathtimer: "+n\PathTimer
				If n\Idle > 0 Then n\Idle = Max(0,n\Idle-FPSfactor)
				
				Select n\State
					Case 2 ;tries to kill the player
						If KillTimer < 0 Then n\State = 0
						
						If n\State2 =< 0 Then
							temp = False
							If dist < 8.0 Then 
								PositionEntity pvt, EntityX(n\Collider), EntityY(n\Collider) + 0.6, EntityZ(n\Collider)
								PointEntity pvt, Collider
								EntityPick(pvt,8.0-CrouchState)
								
								If PickedEntity() = Collider Then 
									temp = True
									n\State2 = 140
								Else
									If n\LastSeen < 70*25 Then
										dist2# = 4.0
										For w.WayPoints = Each WayPoints
											If w\room = PlayerRoom Then
												dist2# = Distance(EntityX(Collider),EntityZ(Collider),EntityX(w\obj,True),EntityZ(w\obj,True))
												If dist2 > 0.7*((n\ID Mod 3)+1) Then
													n\EnemyX = EntityX(w\obj,True)
													n\EnemyY = EntityY(Collider)
													n\EnemyZ = EntityZ(w\obj,True)													
													Exit
												EndIf
											EndIf
										Next
									Else
										n\EnemyX = EntityX(Collider)
										n\EnemyY = EntityY(Collider)
										n\EnemyZ = EntityZ(Collider)										
									EndIf
								EndIf
							EndIf
						Else
							temp = True
						EndIf
						
						If temp = True Then
							If n\LastSeen > 0 And n\LastSeen < 70*15 Then
								If n\Sound <> 0 Then FreeSound n\Sound : n\Sound = 0
								n\Sound = LoadSound("SFX\MTF\ThereHeIs"+Rand(1,3)+".ogg")
								PlaySound2(n\Sound, Camera, n\Collider, 10.0)
								PlayMTFMessage(n\Sound)
							EndIf
							
							DebugLog "player in sight"
							n\PathStatus = 0
							;n\PathTimer = 0
							n\LastSeen = (70*35)
							
							If n\State3 = 0 Then
								n\SoundChn = PlaySound2(MTFSFX(Rand(0,2)),Camera,n\Collider, 7.0)
								PlayMTFMessage(MTFSFX(Rand(0,2)))
								n\State3 = 70*145
								n\Reload = 200
								For n2.NPCs = Each NPCs
									If n2\Target = n Then
										n2\State3 = 70*145
										n2\Reload = 200
									EndIf
								Next
								If n\Target <> Null Then n\Target\State3 = 70*145 : n\Target\Reload = 200
							EndIf
							
							PositionEntity(pvt, EntityX(n\Collider), EntityY(n\Collider) + 0.6, EntityZ(n\Collider))
							PointEntity(pvt, Collider)
							;RotateEntity(pvt, Min(EntityPitch(pvt), 40), EntityYaw(pvt), 0)
							
							RotateEntity(n\Collider, EntityPitch(n\Collider), CurveAngle(EntityYaw(pvt), EntityYaw(n\Collider), 10.0), 0, True)
							
							If n\Reload = 0 Then
								;Animate2(n\obj, AnimTime(n\obj), 110, 120, 0.2, False)
								
								EntityPick(pvt, 7.0)
								If PickedEntity() = Collider Then
									prev% = KillTimer
									
									PlaySound2(GunshotSFX, Camera, n\Collider, 35)
									
									RotateEntity(pvt, EntityPitch(n\Collider), EntityYaw(n\Collider), 0, True)
									PositionEntity(pvt, EntityX(n\obj), EntityY(n\obj), EntityZ(n\obj))
									MoveEntity (pvt,0.8*0.079, 10.75*0.079, 6.9*0.079)
									
									PointEntity pvt, Collider
									Shoot(EntityX(pvt),EntityY(pvt),EntityZ(pvt),EntityPitch(pvt)-Rnd(-3,3),EntityYaw(pvt)-Rnd(-3,3),dist+0.5, False)
									n\Reload = 10
									
									;player killed -> "target terminated"
									If prev => 0 And KillTimer < 0 Then
										If n\Sound <> 0 Then FreeSound n\Sound : n\Sound = 0
										n\Sound = LoadSound("SFX\MTF\Targetterminated"+Rand(1,3)+".ogg")
										n\SoundChn = PlaySound2(n\Sound, Camera, n\Collider, 10.0)
										PlayMTFMessage(n\Sound)
									EndIf
								EndIf
								
							End If
							
							If dist > 1.7 And n\State2 =< 0 And n\Idle=0 Then
								n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 20.0)
								MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
								Animate2(n\obj, AnimTime(n\obj), 402, 431, n\CurrSpeed*26)
							Else
								Animate2(n\obj, AnimTime(n\obj), 110, 120, 0.2, False)
								n\CurrSpeed = CurveValue(0, n\CurrSpeed, 20.0)
							EndIf
						Else ;player not visible
							If dist < 50.0 Then
								n\CurrSpeed = 0
								If n\PathTimer =< 0 And Distance(EntityX(n\Collider),EntityZ(n\Collider),n\EnemyX,n\EnemyZ) Then
									n\PathStatus = FindPath(n, n\EnemyX,n\EnemyY+0.1,n\EnemyZ)
									
									If n\PathStatus = 1 Then 
										n\PathTimer = Rnd(10,15)*70
										n\State = 3 
										DebugLog "n\state -> 3"
									ElseIf n\PathStatus = 2
										n\PathTimer = Rnd(15,25)*70
									Else
										n\PathTimer = Rnd(5,10)*70
									EndIf
								Else
									n\PathTimer = Max(n\PathTimer-FPSfactor,0)
									
									Animate2(n\obj, AnimTime(n\obj), 299, 349, 0.5)
									RotateEntity(n\Collider, Sin(MilliSecs() / 25) * 1.5, CurveAngle(n\Angle + n\PrevState + Sin(MilliSecs() / 100) * 100, EntityYaw(n\Collider), 50), 0, True)
								EndIf	
							Else
								n\LastSeen=0
							EndIf
							
							If n\LastSeen =< 0 Then ;player lost
								If n\Target = Null Then
									If n\Sound <> 0 Then FreeSound n\Sound : n\Sound = 0
									n\Sound = LoadSound("SFX\MTF\Targetlost"+Rand(1,2)+".ogg")
									PlaySound2(n\Sound, Camera, n\Collider, 10.0)
									PlayMTFMessage(n\Sound)
								EndIf
								n\State = 0 : n\PathStatus = 0 : n\PathLocation = 0
								If n\Target <> Null Then
									n\State = 4
								EndIf
							EndIf
						EndIf
					Case 3 ;following a path
						
						If n\PathStatus = 2 Then
							n\State = 0
							n\CurrSpeed = 0
						ElseIf n\PathStatus = 1
							If n\Path[n\PathLocation]=Null Then 
								If n\PathLocation > 19 Then 
									n\PathLocation = 0
									n\PathStatus = 0
									If n\LastSeen > 0 Then n\State = 2 : n\PathTimer = 0 ;etsimässä pelaajaa, palataan takaisin "hyökkäysmoodiin"
								Else
									n\PathLocation = n\PathLocation + 1
								EndIf
							Else
								If n\Path[n\PathLocation]\door <> Null Then
									If n\Path[n\PathLocation]\door\open = False Then
										n\Path[n\PathLocation]\door\open = True
										n\Path[n\PathLocation]\door\timerstate = 8.0*70.0
										PlaySound2(MTFSFX(5),Camera, n\Path[n\PathLocation]\door\obj, 8.0)
										PlayMTFMessage(MTFSFX(5))
									EndIf
								EndIf
								
								PointEntity n\obj, n\Path[n\PathLocation]\obj
								
								dist2# = EntityDistance(n\Collider,n\Path[n\PathLocation]\obj) 
								
								RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj)+n\Angle, EntityYaw(n\Collider), 10.0), 0
								If n\Idle = 0 Then
									n\CurrSpeed = CurveValue(n\Speed, (0.5+Min(0.5,n\LastSeen))*n\Speed*Max(Min(dist2,1.0),0.2), 20.0)
									MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
								EndIf
								
								If dist2 < (0.25+(n\Path[Min(n\PathLocation+1,19)]=Null)*0.25) Then
									n\PathLocation = n\PathLocation + 1
								EndIf 
							EndIf
						Else
							n\CurrSpeed = 0
							n\State = 0
						EndIf
						
						If n\Idle = 0 Then
							If n\LastSeen > 0 Then 
								Animate2(n\obj, AnimTime(n\obj), 402, 431, n\CurrSpeed*30)
							Else
								Animate2(n\obj, AnimTime(n\obj), 121, 150, n\CurrSpeed*30)
							EndIf
						Else
							Animate2(n\obj, AnimTime(n\obj), 110, 120, 0.2, False)
							n\CurrSpeed = CurveValue(0, n\CurrSpeed, 20.0)
						EndIf
						
						n\Angle = CurveValue(0,n\Angle,25.0)
					Case 4 ;following another team member
						If n\Target <> Null Then
							n\Reload = 200
							PointEntity n\obj, n\Target\obj
							
							RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj)+n\Angle, EntityYaw(n\Collider), 10.0), 0
							
							dist# = EntityDistance(n\obj,n\Target\obj)
							
							n\CurrSpeed = CurveValue(n\Speed*Min(1.0,(dist-1.0)*3), n\CurrSpeed, 20.0)
							MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
							
							If dist > 7.0 Then
								If n\PathTimer =< 0 Then
									DebugLog "seurattava hukattu -> etsitään reitti"
									n\PathStatus = FindPath(n, EntityX(n\Target\Collider),EntityY(n\Target\Collider)+0.3,EntityZ(n\Target\Collider))
									n\PathTimer = Rnd(10.0,14.0)*70.0
									If n\PathStatus = 1 Then n\State = 3
								Else
									n\PathTimer = Max(n\PathTimer-FPSfactor,0)
								EndIf
							EndIf
						Else
							n\State = 0
						EndIf
						
						If Abs(n\CurrSpeed)<0.01 Then  
							Animate2(n\obj, AnimTime(n\obj), 165, 256, 0.5)
						Else
							Animate2(n\obj, AnimTime(n\obj), 121, 150, n\CurrSpeed*20)
						EndIf
						
						n\Angle = CurveValue(0,n\Angle,30.0)
					Case 5 ;shooting at some other target than the player
						target=CreatePivot()
						PositionEntity target, n\EnemyX, n\EnemyY, n\EnemyZ, True
						
						Animate2(n\obj, AnimTime(n\obj), 110, 120, 0.2, False)
						
						If Abs(EntityX(target)-EntityX(n\Collider)) < 55.0 Then
							If Abs(EntityZ(target)-EntityZ(n\Collider)) < 55.0 Then
								If Abs(EntityY(target)-EntityY(n\Collider))< 20.0 Then
									PointEntity n\obj, target
									RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),90.0), 0, True
									;PositionEntity(n\Collider, EntityX(n\Collider), CurveValue(EntityY(target)+8.0,EntityY(n\Collider),70.0), EntityZ(n\Collider))
									
									If n\PathTimer = 0 Then
										n\PathStatus = EntityVisible(n\Collider,target)
										n\PathTimer = Rand(100,200)
									Else
										n\PathTimer = Min(n\PathTimer-FPSfactor,0.0)
									EndIf
									
									If n\PathStatus = 1 Then
										If n\Reload =< 0 Then
											dist# = Distance(EntityX(target),EntityZ(target),EntityX(n\Collider),EntityZ(n\Collider))
											
											If dist<20.0 Then
												PositionEntity pvt, EntityX(n\obj),EntityY(n\obj), EntityZ(n\obj)
												RotateEntity pvt, EntityPitch(n\Collider), EntityYaw(n\Collider),0
												MoveEntity (pvt,0.8*0.079, 10.75*0.079, 6.9*0.079)
												
												If WrapAngle(EntityYaw(pvt)-EntityYaw(n\Collider))<5 Then
													;RotateEntity(p\pvt, EntityPitch(n\Collider), EntityYaw(n\Collider), 0)
													;MoveEntity(pvt, 0.0, 0.80, 0.45)
													PointEntity pvt, Collider
													Shoot(EntityX(pvt),EntityY(pvt),EntityZ(pvt),EntityPitch(pvt)-Rnd(-3,3),EntityYaw(pvt)-Rnd(-4,4),0.1, False)
													n\Reload = 10
												EndIf
												
											EndIf
										EndIf
									EndIf
									
									n\Reload=n\Reload-FPSfactor
									
								EndIf
							EndIf
						EndIf		
						
						FreeEntity target
					Default
						n\PathStatus = 0
						n\CurrSpeed = 0
						
						Animate2(n\obj, AnimTime(n\obj), 165, 256, 0.5)
						
						n\PathTimer = Max(n\PathTimer-FPSfactor,0)
						
						If Rand(400) = 1 Then 
							If n\PathTimer <= 0 Then
								For r.Rooms = Each Rooms
									If Distance(EntityX(n\Collider),EntityZ(n\Collider),EntityX(r\obj),EntityZ(r\obj))<4.0 Then
										n\PathStatus = FindPath(n, EntityX(r\obj)+Sin(r\angle)*3.5*(n\PrevX-1.0), 0.2, EntityZ(r\obj)-Cos(r\angle)*3.5*(n\PrevX-1.0))
										
										Exit
									EndIf
								Next
								n\PathTimer = 70*10
								
								n\Angle = Rnd(360)								
							Else
								
							EndIf
							
						EndIf
						
						RotateEntity(n\Collider, Sin(MilliSecs() / 25) * 1.5, CurveAngle(n\Angle + n\PrevState + Sin(MilliSecs() / 100) * 100, EntityYaw(n\Collider), 50), 0, True)
				End Select
				
				n\LastSeen = Max(n\LastSeen-FPSfactor,0)
				
				If n\LastSeen<70*20 And n\LastSeen+FPSfactor=>70*20 Then
					If Rand(2)=1 Then 
						If n\Sound <> 0 Then FreeSound n\Sound : n\Sound = 0
						n\Sound = LoadSound("SFX\MTF\Searching"+Rand(1,3)+".ogg")
						PlaySound2(n\Sound, Camera, n\Collider, 10.0)
						PlayMTFMessage(n\Sound)
					EndIf
				EndIf
				
				n\State2=Max(n\State2-FPSfactor,0)
				
				n\State3=Max(n\State3-FPSfactor,0)
				If n\Target = Null Then 
					If n\State3 < 70*120 And n\State3+FPSfactor => 70*120  And FPSfactor <> 0 Then
						PlaySound2 (MTFSFX(Rand(3,4)),Camera,n\Collider, 8.0)
					EndIf
				EndIf
				
				If n\CurrSpeed > 0.01 Then
					If prevframe < 137 And AnimTime(n\obj)=>137 Then
						PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.5,0.7))						
					ElseIf prevframe < 122 And AnimTime(n\obj)=>122
						PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.5,0.7))						
					ElseIf prevframe < 408 And AnimTime(n\obj)=>408
						PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.8,1.0))						
					ElseIf prevframe < 423 And AnimTime(n\obj)=>423
						PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.8,1.0))
					EndIf
				EndIf
				
				If n\State <> 5 Then 
					If Rand(10)=1 Then 
						If EntityY(n\Collider)<-20.0 Then
							For r.Rooms = Each Rooms
								If Abs(EntityX(n\Collider)-EntityX(r\obj))<4.0 Then
									If Abs(EntityZ(n\Collider)-EntityZ(r\obj))<4.0 Then
										PositionEntity n\Collider, EntityX(r\obj), 0.5, EntityZ(r\obj)
										ResetEntity n\Collider
										Exit
									EndIf
								EndIf
							Next
						EndIf
						
						For d.Doors = Each Doors
							If d\open = False Then
								If Abs(EntityX(d\obj,True)-EntityX(n\Collider))<1.5 Then
									If Abs(EntityZ(d\obj,True)-EntityZ(n\Collider))<1.5 Then
										UseDoor(d, False)
										Exit
									EndIf
								EndIf
							EndIf
						Next
						
						If n\PrevY = 0 Then 
							If Curr106\State < -10 Then 
								If Abs(EntityX(n\Collider)-EntityX(Curr106\Collider))<4.0 Then
									If Abs(EntityZ(n\Collider)-EntityZ(Curr106\Collider))<4.0 Then
									;n\PrevY = 70*30
										TempSound = LoadSound("SFX\MTF\Oldman3.ogg")
										n\SoundChn = PlaySound2 (TempSound,Camera,n\Collider, 8.0)
										PlayMTFMessage(TempSound)
										For n2.NPCs = Each NPCs
											If n2\NPCtype = NPCtypeMTF Then
												n2\PrevY = 70*30
											EndIf
										Next
									EndIf
								EndIf
							EndIf
						Else
							n\PrevY = Max(0,n\PrevY-FPSfactor)
						EndIf
						
						PositionEntity pvt, EntityX(n\Collider), EntityY(n\Collider) + 0.6, EntityZ(n\Collider)
						PointEntity pvt, Collider
						EntityPick(pvt, 0.4)
						
						If PickedEntity() <> 0 Then 
							If Rand(2) = 1 Then 
								n\Angle = 90.0
							Else
								n\Angle = -90
							EndIf
						EndIf
					EndIf		
					
					If Rand(10)=1 Then 
						
						;check if player is visible
						temp = False
						If dist < PlayerSoundVolume Then 
							temp = True 
						ElseIf dist < 8.0-(CrouchState*3)+LightVolume
							PositionEntity(pvt, EntityX(n\Collider), EntityY(n\Collider) + 0.6, EntityZ(n\Collider))
							PointEntity(pvt, Collider)
							If WrapAngle(EntityYaw(n\Collider)-EntityYaw(pvt))<90 Then
								If EntityVisible(n\Collider,Collider) Then temp = True 
							EndIf
						EndIf
						
						If temp Then 
							n\LastSeen = (70*35)
							n\State = 2
							n\State2 = 70*6
							n\EnemyX = EntityX(Collider)
							n\EnemyY = EntityY(Collider)
							n\EnemyZ = EntityZ(Collider)
							
							For n2.NPCs = Each NPCs
								If n <> n2 And n2\NPCtype = NPCtypeMTF Then
								;If n2\State2 =< 0 Then
									If Abs(EntityX(n\Collider)-EntityX(n2\Collider))<32.0 Then
										If Abs(EntityZ(n\Collider)-EntityZ(n2\Collider))<32.0 Then		
											n2\LastSeen = (70*35)
											n2\State = 2
											n2\State2 = 70*6
											n2\EnemyX = EntityX(Collider)
											n2\EnemyY = EntityY(Collider)
											n2\EnemyZ = EntityZ(Collider)
										EndIf
									EndIf
								;EndIf
								EndIf
							Next						
						EndIf
						
					EndIf
					
					
				EndIf
				
				FreeEntity pvt
				
				n\Reload = Max(0, n\Reload - FPSfactor)
				
				PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.22, EntityZ(n\Collider))
				
				RotateEntity n\obj, EntityPitch(n\Collider), EntityYaw(n\Collider), 0
			Case NPCtypeD 	;------------------------------------------------------------------------------------------------------------------
				RotateEntity(n\Collider, 0, EntityYaw(n\Collider), 0, True)
				
				prevframe = AnimTime(n\obj)
				
				Select n\State
					Case 0 ;idle
						n\CurrSpeed = CurveValue(0.0, n\CurrSpeed, 5.0)
						Animate2(n\obj, AnimTime(n\obj), 210, 235, 0.1)
					Case 1 ;walking
						n\CurrSpeed = CurveValue(0.015, n\CurrSpeed, 5.0)
						Animate2(n\obj, AnimTime(n\obj), 236, 260, n\CurrSpeed * 18)
				End Select
				
				If n\CurrSpeed > 0.01 Then
					If prevframe < 244 And AnimTime(n\obj)=>244 Then
						PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))						
					ElseIf prevframe < 256 And AnimTime(n\obj)=>256
						PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))
					EndIf
				EndIf
				
				MoveEntity(n\Collider, 0, 0, n\CurrSpeed * FPSfactor)
				
				PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.32, EntityZ(n\Collider))
				
				RotateEntity n\obj, EntityPitch(n\Collider), EntityYaw(n\Collider)-180.0, 0
			Case NPCtype372 ;------------------------------------------------------------------------------------------------------------------
				If PlayerRoom\RoomTemplate\Name <> "pocketdimension" Then 
					If n\Idle Then
						HideEntity(n\obj)
						If Rand(50) = 1 And (BlinkTimer < -5 And BlinkTimer > -15) Then
							ShowEntity(n\obj)
							angle# = EntityYaw(Collider)+Rnd(-90,90)
							
							dist = Rnd(1.5, 2.0)
							PositionEntity(n\Collider, EntityX(Collider) + Sin(angle) * dist, EntityY(Collider)+0.2, EntityZ(Collider) + Cos(angle) * dist)
							n\Idle = False
							n\State = Rand(20, 60)
							
							If Rand(300)=1 Then PlaySound2(RustleSFX(Rand(0,2)),Camera, n\obj, 8, Rnd(0.0,0.2))
						End If
					Else
						PositionEntity(n\obj, EntityX(n\Collider) + Rnd(-0.005, 0.005), EntityY(n\Collider)+0.3+0.1*Sin(MilliSecs()/2), EntityZ(n\Collider) + Rnd(-0.005, 0.005))
						RotateEntity n\obj, 0, EntityYaw(n\Collider), ((MilliSecs()/5) Mod 360)
						
						Animate2(n\obj, AnimTime(n\obj), 32, 113, 0.4)
						
						If EntityInView(n\obj, Camera) Then
							Achievements(Achv372) = True
							
							If Rand(30)=1 Then 
								If (Not ChannelPlaying(n\SoundChn)) Then
									If EntityVisible(Camera, n\obj) Then 
										n\SoundChn = PlaySound2(RustleSFX(Rand(0,2)),Camera, n\obj, 8, 0.3)
									EndIf
								EndIf
							EndIf
							
							temp = CreatePivot()
							PositionEntity temp, EntityX(Collider), EntityY(Collider), EntityZ(Collider)
							PointEntity temp, n\Collider
							
							angle =  WrapAngle(EntityYaw(Collider)-EntityYaw(temp))
							If angle < 180 Then
								RotateEntity n\Collider, 0, EntityYaw(Collider)-80, 0		
							Else
								RotateEntity n\Collider, 0, EntityYaw(Collider)+80, 0
							EndIf
							FreeEntity temp
							
							MoveEntity n\Collider, 0, 0, 0.03*FPSfactor
							
							n\State = n\State-FPSfactor
						EndIf
						n\State=n\State-(FPSfactor/80.0)
						If n\State <= 0 Then n\Idle = True	
					End If
					
				EndIf
				
				n\DropSpeed = 0
				ResetEntity(n\Collider)						
				
			Case NPCtypeApache ;------------------------------------------------------------------------------------------------------------------
				
				dist = EntityDistance(Collider, n\Collider)
				If dist<60.0 Then 
					If PlayerRoom\RoomTemplate\Name = "exit1" Then 
						dist2 = Max(Min(EntityDistance(n\Collider, PlayerRoom\Objects[3])/(8000.0*RoomScale),1.0),0.0)
					Else 
						dist2 = 1.0
					EndIf
					
					n\SoundChn = LoopSound2(ApacheSFX, n\SoundChn, Camera, n\Collider, 25.0, dist2)
				EndIf
				
				n\DropSpeed = 0
				
				Select n\State
					Case 0
					Case 1
						TurnEntity(n\obj2,0,20.0*FPSfactor,0)
						TurnEntity(n\obj3,20.0*FPSfactor,0,0)
						
						If Abs(EntityX(Collider)-EntityX(n\Collider))< 30.0 Then
							If Abs(EntityZ(Collider)-EntityZ(n\Collider))<30.0 Then
								If Abs(EntityY(Collider)-EntityY(n\Collider))<20.0 Then
									If Rand(20)=1 Then 
										If EntityVisible(Collider, n\Collider) Then
											n\State = 2
											PlaySound2(AlarmSFX(2), Camera, n\Collider, 50, 1.0)
										EndIf
									EndIf									
								EndIf
							EndIf
						EndIf
					Case 2,3 ;player located -> attack
						
						If n\State = 2 Then 
							target = Collider
						ElseIf n\State = 3
							target=CreatePivot()
							PositionEntity target, n\EnemyX, n\EnemyY, n\EnemyZ, True
						EndIf
						
						TurnEntity(n\obj2,0,20.0*FPSfactor,0)
						TurnEntity(n\obj3,20.0*FPSfactor,0,0)
						
						If Abs(EntityX(target)-EntityX(n\Collider)) < 55.0 Then
							If Abs(EntityZ(target)-EntityZ(n\Collider)) < 55.0 Then
								If Abs(EntityY(target)-EntityY(n\Collider))< 20.0 Then
									PointEntity n\obj, target
									RotateEntity n\Collider, CurveAngle(Min(WrapAngle(EntityPitch(n\obj)),40.0),EntityPitch(n\Collider),40.0), CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),90.0), EntityRoll(n\Collider), True
									PositionEntity(n\Collider, EntityX(n\Collider), CurveValue(EntityY(target)+8.0,EntityY(n\Collider),70.0), EntityZ(n\Collider))
									
									dist# = Distance(EntityX(target),EntityZ(target),EntityX(n\Collider),EntityZ(n\Collider))
									
									n\CurrSpeed = CurveValue(Min(dist-6.5,6.5)*0.008, n\CurrSpeed, 50.0)
									
									;If Distance(EntityX(Collider),EntityZ(Collider),EntityX(n\collider),EntityZ(n\collider)) > 6.5 Then
									;	n\currspeed = CurveValue(0.08,n\currspeed,50.0)
									;Else
									;	n\currspeed = CurveValue(0.0,n\currspeed,30.0)
									;EndIf
									MoveEntity n\Collider, 0,0,n\CurrSpeed*FPSfactor
									
									
									If n\PathTimer = 0 Then
										n\PathStatus = EntityVisible(n\Collider,target)
										n\PathTimer = Rand(100,200)
									Else
										n\PathTimer = Min(n\PathTimer-FPSfactor,0.0)
									EndIf
									
									If n\PathStatus = 1 Then ;player visible
										RotateEntity n\Collider, EntityPitch(n\Collider), EntityYaw(n\Collider), CurveAngle(0, EntityRoll(n\Collider),40), True
										
										If n\Reload =< 0 Then
											If dist<20.0 Then
												pvt = CreatePivot()
												
												PositionEntity pvt, EntityX(n\Collider),EntityY(n\Collider), EntityZ(n\Collider)
												RotateEntity pvt, EntityPitch(n\Collider), EntityYaw(n\Collider),EntityRoll(n\Collider)
												MoveEntity pvt, 0, 8.87*(0.21/9.0), 8.87*(1.7/9.0) ;2.3
												PointEntity pvt, target
												
												If WrapAngle(EntityYaw(pvt)-EntityYaw(n\Collider))<10 Then
													PlaySound2(Gunshot2SFX, Camera, n\Collider, 20)
													
													Shoot( EntityX(pvt),EntityY(pvt), EntityZ(pvt),EntityPitch(pvt)+Rnd(-2.0,2.0),EntityYaw(n\Collider)+Rnd(-2.0,2.0),20.0,(n\State=2))
													
													n\Reload = 5
												EndIf
												
												FreeEntity pvt
											EndIf
										EndIf
									Else 
										RotateEntity n\Collider, EntityPitch(n\Collider), EntityYaw(n\Collider), CurveAngle(-20, EntityRoll(n\Collider),40), True
									EndIf
									MoveEntity n\Collider, -EntityRoll(n\Collider)*0.002,0,0
									
									n\Reload=n\Reload-FPSfactor
									
									
								EndIf
							EndIf
						EndIf		
						
						If n\State = 3 Then FreeEntity target
					Case 4
						If n\State2 < 300 Then
							
							TurnEntity(n\obj2,0,20.0*FPSfactor,0)
							TurnEntity(n\obj3,20.0*FPSfactor,0,0)
							
							TurnEntity n\Collider,0,-FPSfactor*7,0;Sin(MilliSecs()/40)*FPSfactor
							n\State2=n\State2+FPSfactor*0.3
							
							target=CreatePivot()
							PositionEntity target, n\EnemyX, n\EnemyY, n\EnemyZ, True
							
							PointEntity n\obj, target
							MoveEntity n\obj, 0,0,FPSfactor*0.001*n\State2
							PositionEntity(n\Collider, EntityX(n\obj), EntityY(n\obj), EntityZ(n\obj))
							
							If EntityDistance(n\obj, target) <0.3 Then
								If TempSound2 <> 0 Then FreeSound TempSound2 : TempSound2 = 0
								TempSound2 = LoadSound("SFX\apachecrash2.ogg")
								CameraShake = Max(CameraShake, 3.0)
								PlaySound TempSound2
								n\State = 5
							EndIf
							
							FreeEntity target
						EndIf
				End Select
				
				PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider), EntityZ(n\Collider))
				RotateEntity n\obj, EntityPitch(n\Collider), EntityYaw(n\Collider), EntityRoll(n\Collider), True				
		End Select
		
		If Distance(EntityX(Collider),EntityZ(Collider),EntityX(n\Collider),EntityZ(n\Collider))<HideDistance*0.7 Then 
			TranslateEntity n\Collider, 0, n\DropSpeed, 0
			
			Local CollidedFloor% = False
			For i% = 1 To CountCollisions(n\Collider)
				If CollisionY(n\Collider, i) < EntityY(n\Collider) - 0.01 Then CollidedFloor = True
			Next
			
			If CollidedFloor = True Then
				n\DropSpeed# = 0
			Else
				n\DropSpeed# = Max(n\DropSpeed - 0.001*FPSfactor,-0.1)
			EndIf
		Else
			n\DropSpeed = 0
		EndIf
		
	Next
	
End Function


Function Shoot(x#,y#,z#,pitch#,yaw#,range#=10.0,particles%=True)
	Local pvt = CreatePivot()
	
	PositionEntity pvt, x,y,z
	RotateEntity pvt, pitch, yaw, 0, True
	
	EntityPick pvt, range
	
	;muzzle flash
	Local p.Particles = CreateParticle(x,y,z, 1, Rnd(0.1,0.15), 0.0, 5)
	TurnEntity p\obj, 0,0,Rnd(360)
	p\Achange = -0.15
	
	If (Not GodMode) Then 
		
		If PickedEntity() = Collider Then
			TurnEntity Camera, Rnd(-3,3), Rnd(-3,3), 0
			
			If WearingVest>0 Then
				If WearingVest = 1 Then
					Select Rand(8)
						Case 1,2,3,4,5
							BlurTimer = 500
							Stamina = 0
							Msg = "Air escapes from your lungs as something hits your vest" : MsgTimer = 70*6		
							Injuries = Injuries + Rnd(0.1,0.5)
						Case 6
							BlurTimer = 500
							Msg = "You feel a burning pain in your left leg" : MsgTimer = 70*6
							Injuries = Injuries + Rnd(0.8,1.2)
						Case 7
							BlurTimer = 500
							Msg = "You feel a burning pain in your right leg" : MsgTimer = 70*6		
							Injuries = Injuries + Rnd(0.8,1.2)
						Case 8
							Kill()
					End Select	
				Else
					If Rand(10)=1 Then
						Kill()
					Else
						Msg = "You feel something hitting your vest" : MsgTimer = 70*6	
						Injuries = Injuries + Rnd(0.1,0.5)
					EndIf
				EndIf
			Else
				Select Rand(6)
					Case 1
						Kill()
					Case 2
						BlurTimer = 500
						Msg = "You feel a burning pain in your left leg" : MsgTimer = 70*6
						Injuries = Injuries + Rnd(0.8,1.2)
					Case 3
						BlurTimer = 500
						Msg = "You feel a burning pain in your right leg" : MsgTimer = 70*6	
						Injuries = Injuries + Rnd(0.8,1.2)
					Case 4
						BlurTimer = 500
						Msg = "You feel a burning pain in your right shoulder" : MsgTimer = 70*6			
						Injuries = Injuries + Rnd(0.8,1.2)	
					Case 5
						BlurTimer = 500
						Msg = "You feel a burning pain in your left shoulder" : MsgTimer = 70*6			
						Injuries = Injuries + Rnd(0.8,1.2)	
					Case 6
						BlurTimer = 500
						Msg = "You feel a burning pain in your abdomen" : MsgTimer = 70*6
						Injuries = Injuries + Rnd(2.5,4.0)						
				End Select
			EndIf
			
			Injuries = Min(Injuries, 4.0)
			
			;Kill()
			PlaySound BullethitSFX
		ElseIf PickedEntity() <> 0
			PositionEntity pvt, PickedX(),PickedY(),PickedZ()
			PlaySound2(Gunshot3SFX, Camera, pvt, 0.4, Rnd(0.8,1.0))
			
			If particles Then 
				;dust/smoke particles
				p.Particles = CreateParticle(PickedX(),PickedY(),PickedZ(), 0, 0.03, 0, 80)
				p\speed = 0.001
				p\SizeChange = 0.003
				p\A = 0.8
				p\Achange = -0.01
				RotateEntity p\pvt, EntityPitch(pvt)-180, EntityYaw(pvt),0
				
				For i = 0 To Rand(2,3)
					p.Particles = CreateParticle(PickedX(),PickedY(),PickedZ(), 0, 0.006, 0.003, 80)
					p\speed = 0.02
					p\A = 0.8
					p\Achange = -0.01
					RotateEntity p\pvt, EntityPitch(pvt)+Rnd(170,190), EntityYaw(pvt)+Rnd(-10,10),0	
				Next
				
				;bullet hole decal
				Local de.Decals = CreateDecal(Rand(13,14), PickedX(),PickedY(),PickedZ(), 0,0,0)
				AlignToVector de\obj,-PickedNX(),-PickedNY(),-PickedNZ(),3
				MoveEntity de\obj, 0,0,-0.001
				EntityFX de\obj, 1
				de\lifetime = 70*20
				EntityBlend de\obj, 2
				de\Size = Rnd(0.028,0.034)
				ScaleSprite de\obj, de\Size, de\Size
			EndIf
		EndIf
		
	EndIf
	
	FreeEntity pvt	
End Function

Function PlayMTFMessage(sound%)
	If SelectedItem <> Null Then
		If SelectedItem\state2 = 3 And SelectedItem\state > 0 Then 
			Select SelectedItem\itemtemplate\tempname 
				Case "radio","fineradio","18vradio"
					If RadioCHN(3)<> 0 Then StopChannel RadioCHN(3)
					RadioCHN(3) = PlaySound (sound)
			End Select
		EndIf
	EndIf 
End Function

;~IDEal Editor Parameters:
;~F#6#1F#101#125#856#8C8
;~C#Blitz3D