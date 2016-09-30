// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "Runtime/Engine/Classes/Sound/SoundWaveProcedural.h"
#include "Runtime/Online/HTTP/Public/Http.h"
#include "Components/ActorComponent.h"
#include "MivoqSynthesizer.generated.h"

DECLARE_DYNAMIC_MULTICAST_DELEGATE(FSynthesizedEvent);
DECLARE_DYNAMIC_MULTICAST_DELEGATE(FSynthesisPlayedEvent);

UCLASS( ClassGroup=(Custom), meta=(BlueprintSpawnableComponent) )
class INTERACTIVEGUIDE_API UMivoqSynthesizer : public UActorComponent
{
	GENERATED_BODY()

public:	
	FHttpModule* Http;

	UPROPERTY(BlueprintReadWrite)
		UAudioComponent* AudioComp;

	UPROPERTY(BlueprintReadWrite)
		FString url = "http://inst0213.tts.mivoq.it/";

	UPROPERTY(BlueprintReadWrite, EditAnywhere)
		FString user = "";
	UPROPERTY(BlueprintReadWrite, EditAnywhere)
		FString password = "";

	UPROPERTY(BlueprintReadWrite, EditAnywhere)
		FString textType = "TEXT";

	UPROPERTY(BlueprintReadWrite, EditAnywhere)
		FString language = "it";

	UPROPERTY(BlueprintReadWrite, EditAnywhere)
		FString gender = "male";

	UPROPERTY(BlueprintReadWrite, EditAnywhere)
		FString name = "roberto-hsmm";

	UPROPERTY(BlueprintReadWrite, EditAnywhere)
		FString age = "35";

	UPROPERTY(BlueprintReadWrite, EditAnywhere)
		FString variant = "1";

	UPROPERTY(BlueprintReadWrite, EditAnywhere)
		FString style = "";

	UPROPERTY(BlueprintReadWrite, EditAnywhere)
		FString effects = "";

	UPROPERTY()
		TArray<uint8> data;

	// Sets default values for this component's properties
	UMivoqSynthesizer();

	/* The actuall HTTP call */
	UFUNCTION(BlueprintCallable, Category = "Synthesis")
		void GetSynthesis(FString SynthText);

	UFUNCTION(BlueprintCallable, Category = "Synthesis")
		void PlaySynthesis();

	UFUNCTION(BlueprintCallable, Category = "Synthesis")
		void SetAudioComponent(UAudioComponent* Audio);

	/*Assign this function to call when the GET request processes sucessfully*/
	void OnResponseReceived(FHttpRequestPtr Request, FHttpResponsePtr Response, bool bWasSuccessful);

	void ProceduralWaveUnderflow(USoundWaveProcedural* InProceduralWave, int32 SamplesRequired);

	// Called when the game starts
	virtual void BeginPlay() override;

	// Called every frame
	virtual void TickComponent(float DeltaTime, ELevelTick TickType, FActorComponentTickFunction* ThisTickFunction) override;

	UPROPERTY(BlueprintAssignable, Category = "Interaction")
		FSynthesizedEvent SynthesisPlaying;

	UPROPERTY(BlueprintAssignable, Category = "Interaction")
		FSynthesisPlayedEvent SynthesisPlayed;

	static const uint32 SAMPLE_SIZE = sizeof(uint16);

		
	
};
