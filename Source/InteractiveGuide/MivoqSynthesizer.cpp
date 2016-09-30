// Fill out your copyright notice in the Description page of Project Settings.

#include "InteractiveGuide.h"
#include "MivoqSynthesizer.h"
#include "Base64.h"
using namespace std;


// Sets default values for this component's properties
UMivoqSynthesizer::UMivoqSynthesizer()
{
	// Set this component to be initialized when the game starts, and to be ticked every frame.  You can turn these features
	// off to improve performance if you don't need them.
	bWantsBeginPlay = true;
	//PrimaryComponentTick.bCanEverTick = true;

	//When the object is constructed, Get the HTTP module
	Http = &FHttpModule::Get();
}


// Called when the game starts
void UMivoqSynthesizer::BeginPlay()
{
	Super::BeginPlay();
}


void UMivoqSynthesizer::GetSynthesis(FString SynthText)
{
	TSharedRef<IHttpRequest> Request = Http->CreateRequest();
	Request->OnProcessRequestComplete().BindUObject(this, &UMivoqSynthesizer::OnResponseReceived);
	//This is the url	
	FString request = url + "process?";

	request += "INPUT_TYPE=";
	request += textType;
	request += "&INPUT_TEXT=";
	request += FGenericPlatformHttp::UrlEncode(SynthText);
	request += "&LOCALE=" + language;
	request += "&OUTPUT_TYPE=AUDIO";
	request += "&AUDIO=WAVE_FILE";
	request += "&VOICE=" + name; //roberto-hsmm

	GEngine->AddOnScreenDebugMessage(-1, 15.0f, FColor::Red, request);

	Request->SetURL(request);
	Request->SetVerb("GET");
	Request->SetHeader(TEXT("Authorization"), "Basic " + FBase64::Encode(user + ":" + password));
	Request->ProcessRequest();
}

void UMivoqSynthesizer::SetAudioComponent(UAudioComponent* Audio)
{
	AudioComp = Audio;
}

void UMivoqSynthesizer::OnResponseReceived(FHttpRequestPtr Request, FHttpResponsePtr Response, bool bWasSuccessful)
{
	if (bWasSuccessful)
		GEngine->AddOnScreenDebugMessage(-1, 15.0f, FColor::Red, "Response callback: Ok");
	else
		GEngine->AddOnScreenDebugMessage(-1, 15.0f, FColor::Red, "Response callback: Nope");

	if (Response->GetContentType().Contains(TEXT("text/html")))
	{
		GEngine->AddOnScreenDebugMessage(-1, 15.0f, FColor::Red, Response->GetContentAsString());
	}

	// Get the content
	data.AddZeroed(16000);
	data.Append(Response->GetContent());

	GEngine->AddOnScreenDebugMessage(-1, 15.0f, FColor::Blue, FString::FromInt(data.Num()));

	GEngine->AddOnScreenDebugMessage(-1, 15.0f, FColor::Red, Response->GetContentType());

	SynthesisPlaying.Broadcast();
}

void UMivoqSynthesizer::PlaySynthesis()
{
	USoundWaveProcedural* SyntheticVoice = NewObject<USoundWaveProcedural>();

	SyntheticVoice->SampleRate = 16000;
	SyntheticVoice->NumChannels = 1;
	SyntheticVoice->Duration = INDEFINITELY_LOOPING_DURATION;
	SyntheticVoice->SoundGroup = SOUNDGROUP_Voice;
	SyntheticVoice->bLooping = false;
	SyntheticVoice->OnSoundWaveProceduralUnderflow = FOnSoundWaveProceduralUnderflow::CreateUObject(this, &UMivoqSynthesizer::ProceduralWaveUnderflow);

	AudioComp->SetSound(SyntheticVoice);

	GEngine->AddOnScreenDebugMessage(-1, 15.0f, FColor::Yellow, FString::FromInt(data.Num()));

	SyntheticVoice->QueueAudio(data.GetData(), data.Num() * sizeof(int8));
	data.Empty();

	AudioComp->Play();
}

void UMivoqSynthesizer::ProceduralWaveUnderflow(USoundWaveProcedural* InProceduralWave, int32 SamplesRequired)
{
	TArray<uint8> silence;
	data.AddZeroed(SamplesRequired);

	InProceduralWave->QueueAudio(data.GetData(), SamplesRequired * sizeof(int8));
}

// Called every frame
void UMivoqSynthesizer::TickComponent(float DeltaTime, ELevelTick TickType, FActorComponentTickFunction* ThisTickFunction)
{
	Super::TickComponent(DeltaTime, TickType, ThisTickFunction);

	// ...
}


