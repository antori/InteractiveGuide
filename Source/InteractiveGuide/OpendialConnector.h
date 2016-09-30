// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "Components/ActorComponent.h"
#include "Sockets.h"
#include <string>
#include "OpendialConnector.generated.h"

DECLARE_DYNAMIC_MULTICAST_DELEGATE(FOpendialEvent);

UCLASS( ClassGroup=(Custom), meta=(BlueprintSpawnableComponent) )
class INTERACTIVEGUIDE_API UOpendialConnector : public UActorComponent
{
	GENERATED_BODY()

public:	
	FSocket* OpendialSocket;

	UPROPERTY(Category = Opendial, EditAnywhere, BlueprintReadWrite) FString Message;
	UPROPERTY(Category = Opendial, EditAnywhere, BlueprintReadWrite) int32 port;

	// Sets default values for this component's properties
	UOpendialConnector();

	// Called when the game starts
	virtual void BeginPlay() override;

	// Called every frame
	virtual void TickComponent(float DeltaTime, ELevelTick TickType, FActorComponentTickFunction* ThisTickFunction) override;

	FString StringFromBinaryArray(const TArray<uint8>& BinaryArray);

	UPROPERTY(BlueprintAssignable, Category = "Interaction")
		FOpendialEvent IncomingMessage;
};
