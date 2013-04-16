

Dim ParticleTextures%(10)

Type Particles
	Field obj%, pvt%
	Field image%
	
	Field R#, G#, B#, A#, size#
	Field speed#, yspeed#, gravity#
	Field Rchange#, Gchange#, Bchange#, Achange#
	Field SizeChange#
	
	Field lifetime#
End Type 
	
Function CreateParticle.Particles(x#, y#, z#, image%, size#, gravity# = 1.0, lifetime% = 200)
	Local p.Particles = New Particles
	p\lifetime = lifetime
	
	p\obj = CreateSprite()
	PositionEntity(p\obj, x, y, z, True)
	EntityTexture(p\obj, ParticleTextures(image))
	RotateEntity(p\obj, 0, 0, Rnd(360))
	EntityFX(p\obj, 1 + 8)
	
	Select image
		Case 0, 5, 6
			EntityBlend(p\obj, 1)
		Case 1,2,3,4
			EntityBlend(p\obj, BLEND_ADD)
	End Select
	
	p\pvt = CreatePivot()
	PositionEntity(p\pvt, x, y, z, True)
	
	p\image = image
	p\gravity = gravity * 0.004
	p\R = 255 : p\G = 255 : p\B = 255 : p\A = 1.0
	p\size = size
	Return p
End Function
	
Function UpdateParticles()
	For p.Particles = Each Particles
		MoveEntity(p\pvt, 0, 0, p\speed * FPSfactor)
		If p\gravity <> 0 Then p\yspeed = p\yspeed - p\gravity * FPSfactor
		TranslateEntity(p\pvt, 0, p\yspeed, 0, True)
		
		PositionEntity(p\obj, EntityX(p\pvt,True), EntityY(p\pvt,True), EntityZ(p\pvt,True), True)
		
		p\LifeTime=p\lifetime-FPSfactor
		
		TurnEntity(p\obj, 0, 0, FPSfactor)
		
		;entityColor (obj, R, G, B)
		EntityAlpha(p\obj, p\A)
		ScaleSprite p\obj, p\size, p\size
		
		;DebugLog R + ", " + G + ", " + B
		;R:+Rchange * FPSfactor
		;G:+Gchange * FPSfactor
		;B:+Bchange * FPSfactor
		p\A=Min(Max(p\a+p\Achange * FPSfactor,0.0),1.0)
		
		p\size= p\size+p\SizeChange * FPSfactor
		
		If p\lifetime <= 0 Or p\size < 0.00001 Or p\a =< 0 Then
			RemoveParticle(p)
		End If
	Next
End Function
	
Function RemoveParticle(p.Particles)
	FreeEntity(p\obj)
	FreeEntity(p\pvt)	
	Delete p
End Function

Global InSmoke%
Global HissSFX% = LoadSound("SFX\hiss.ogg")

Type Emitters
	Field Obj%
	
	Field Size#
	Field MinImage%, MaxImage%
	Field Gravity#
	Field LifeTime%
	
	Field Disable%
	
	Field Room.Rooms
	
	Field SoundCHN%
	
	Field Speed#, RandAngle#
	Field SizeChange#, Achange#
End Type 

Function UpdateEmitters()
	InSmoke = False
	For e.emitters = Each Emitters
		If FPSfactor > 0 And PlayerRoom = e\Room Then
			;If EntityDistance(Camera, e\Obj) < 6.0 Then
				Local p.Particles = CreateParticle(EntityX(e\obj, True), EntityY(e\obj, True), EntityZ(e\obj, True), Rand(e\minimage, e\maximage), e\size, e\gravity, e\lifetime)
				p\speed = e\speed
				RotateEntity(p\pvt, EntityPitch(e\Obj, True), EntityYaw(e\Obj, True) , EntityRoll(e\Obj, True) , True)
				TurnEntity(p\pvt, Rnd(-e\RandAngle, e\RandAngle), Rnd(-e\RandAngle, e\RandAngle), 0)
				
				p\SizeChange = e\SizeChange
				
				p\Achange = e\achange
				
				e\SoundCHN = LoopSound2(HissSFX, e\SoundCHN, Camera, e\Obj)
				
				;If HissCHN = Null Then
				;	HissCHN = PlaySound2(HissSFX, camera, Obj, 5, 1.0, 0.0)
				;Else
				;	If Not ChannelPlaying(HissCHN) Then HissCHN = PlaySound2(HissSFX, Camera, Obj, 5, 1.0, 0.0)
				;End If
				
				If Not WearingGasMask Then
					Local dist# = Distance(EntityX(Camera, True), EntityZ(Camera, True), EntityX(e\obj, True), EntityZ(e\obj, True))
					If dist < 0.8 Then InSmoke = True
				EndIf
			;EndIf
		End If
	Next
	
	If InSmoke Then
		If EyeIrritation > (70 * 6) Then BlurVolume = Max(BlurVolume, (EyeIrritation - (70 * 6)) / (70.0 * 24.0))
		If EyeIrritation > (70 * 24) Then Kill()
		
		If KillTimer => 0 Then 
			If Rand(150) = 1 Then
				If CoughCHN = 0 Then
					CoughCHN = PlaySound(CoughSFX(Rand(0, 2)))
				Else
					If Not ChannelPlaying(CoughCHN) Then CoughCHN = PlaySound(CoughSFX(Rand(0, 2)))
				End If
			EndIf
		EndIf
		
		EyeIrritation=EyeIrritation+FPSfactor * 4
	EndIf	
End Function 

	
Function CreateEmitter.Emitters(x#, y#, z#, emittertype%) 
	Local e.Emitters = New Emitters
		
	e\Obj = CreatePivot()
	PositionEntity(e\Obj, x, y, z, True)
		
	Select emittertype
		Case 0 ;savu
			e\Size = 0.03
			e\Gravity = -0.2
			e\LifeTime = 200
			e\SizeChange = 0.005
			e\Speed = 0.004
			e\RandAngle = 20
			e\Achange = -0.008
		Case 1
			e\Size = 0.03
			e\Gravity = -0.2
			e\LifeTime = 200
			e\SizeChange = 0.008
			e\Speed = 0.004
			e\RandAngle = 40
			e\Achange = -0.01
			
			e\MinImage = 6 : e\MaxImage = 6
	End Select
	
	For r.Rooms = Each Rooms
		If Abs(EntityX(e\Obj) - EntityX(r\obj)) < 4.0 And Abs(EntityZ(e\Obj) - EntityZ(r\obj)) < 4.0 Then
			e\Room = r
		EndIf
	Next
	
	Return e
		
End Function
	
	

;~IDEal Editor Parameters:
;~F#10#2B#49#52#64
;~C#Blitz3D