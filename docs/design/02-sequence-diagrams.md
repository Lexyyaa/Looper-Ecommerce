# 시퀀스 다이어그램
> 본 문서는 이커머스 기능 구현을 위한 도메인 간 상호작용 흐름을 정리한 문서입니다.
> 기능별 시나리오를 시퀀스 다이어그램으로 시각화 하여 흐름과 예외상황을 파악할 수 있도록 구성하였습니다.

## 사용자
#### 회원가입 (POST /api/v1/users)
```mermaid
sequenceDiagram
    actor Client as Client
    participant 사용자 as 사용자

    Client ->> 사용자: 회원가입 요청 (POST /api/v1/users)
    activate 사용자
    alt 비정상 요청
        alt 필수 입력값 누락
             사용자 -->> Client: 400 Bad Request
        else 필드 규칙 불일치
            사용자 -->> Client: 400 Bad Request
        end
    deactivate 사용자
    else 정상 요청
        activate 사용자
        사용자 ->> 사용자: ID 중복 체크
       alt 이미 존재하는 ID
            사용자 -->> Client: 409 Conflict
       else 회원가입가능
            사용자 ->> 사용자 : 회원정보 저장
            alt 시스템 장애
              사용자 -->> Client: 500 Internal Server Error
            else
               사용자 ->> 사용자 : 회원정보 반환
               사용자 ->> Client : 200 ok 회원정보 응답
            end
        end
    end
    deactivate 사용자
```
#### 내정보조회 (POST /api/v1/users/me)
```mermaid
sequenceDiagram
    actor Client as Client
    participant 사용자 as 사용자


    Client ->> 사용자: 내정보조회 요청 (POST /api/v1/users/me)
    activate 사용자
      사용자 ->> 사용자: 내정보조회
      사용자 ->> 사용자: ID 존재여부 확인
      alt 조회 실패
          alt 존재하지않는 사용자
              사용자 -->> Client: 404 Not Found
          else 시스템 장애
              사용자 -->> Client: 500 Internal Server Error
          end
      else 조회 성공
          사용자 -->> 사용자 : 사용자 정보 반환
          사용자 -->> Client:  200 ok 사용자 정보 응답
      end
    deactivate 사용자
```
---

## 포인트
#### 포인트 충전(POST /api/v1/points/charge)

```mermaid
  sequenceDiagram
    actor Client as Client
    participant 포인트 as 포인트

    Client ->> 포인트: 포인트충전 요청 (POST /api/v1/points/charge)
    activate 포인트
    포인트 ->> 포인트: ID 존재여부 확인
    deactivate 포인트
    alt 충전 실패
        alt 존재하지않는 사용자
            포인트 -->> Client: 404 Not Found
        else 요청금액이 양의 정수가 아님
            포인트 -->> Client: 400 Bad Request
        else 시스템 장애
            포인트 -->> Client: 500 Internal Server Error
        end
    else 충전 성공
        activate 포인트
        포인트 ->> 포인트: 포인트 충전
        포인트 -->> Client: `loginId`, `보유포인트 잔액`을 반환
        deactivate 포인트
    end
```
#### 포인트 조회 (GET /api/v1/points)
```mermaid
  sequenceDiagram
    actor Client as Client
    participant 포인트 as 포인트

    Client ->> 포인트: 포인트조회 요청 (POST /api/v1/points)
    activate 포인트
    포인트 ->> 포인트: ID 존재여부 확인
    deactivate 포인트
    alt 조회 실패
        alt 존재하지않는 사용자
            포인트 -->> Client: 404 Not Found
        else 시스템 장애
            포인트 -->> Client: 500 Internal Server Error
        end
    else 조회 성공
        activate 포인트
        포인트 ->> 포인트: 포인트 조회
        포인트 -->> Client: `loginId`, `보유포인트 잔액`을 반환
        deactivate 포인트
    end
``` 
---
## 브랜드 & 상품
#### 브랜드정보 조회 (GET /api/v1/brands/{brandId})

```mermaid
sequenceDiagram
    actor Client as Client
    participant 브랜드 as 브랜드

    activate 브랜드
    Client ->> 브랜드: 브랜드정보 요청 (GET /api/v1/brands/{brandId})
    브랜드 ->> 브랜드: 브랜드를 존재여부 및 상태 확인
    alt 조회 실패 
        alt 존재하지 않는 브랜드
            브랜드 -->> Client: 404 Not Found
        else 브랜드 상태가 `조회가능` 상태가 아닌 경우 
            브랜드 -->> Client: 400 Bad Request
        else 시스템 장애
            브랜드 -->> Client: 500 Internal Server Error
        end
    else 조회 성공 
        브랜드 -->> Client: 200 ok `브랜드ID`, `브랜드명`,`설명`을 반환
    end
    deactivate 브랜드
```

#### 상품 목록 정보 조회 (GET /api/v1/products)

```mermaid
sequenceDiagram
    actor Client as Client
    participant 상품 as 상품
    participant 브랜드 as 브랜드
    participant 상품_좋아요 as 상품.좋아요

    Client ->> 상품: 상품목록정보조회 요청 (GET /api/v1/products)
    alt 존재하지 않는 정렬값
        상품 -->> Client: 400 Bad Request
    else
        activate 브랜드
        상품 ->> 브랜드: 브랜드를 존재여부 및 상태 확인 
        alt 존재하지 않는 브랜드ID
            브랜드 -->> Client: 404 Not Found
        else
            브랜드 -->> 상품 : 브랜드 정보 반환
            deactivate 브랜드
            activate 상품_좋아요
            상품 ->> 상품_좋아요 : 상품별 좋아요 수 조회
            상품_좋아요 -->> 상품 : 상품별 좋아요 수 합산 
            deactivate 상품_좋아요
            activate 상품
            상품 ->> 상품: `판매중` 상태 상품만 필터링
            상품 ->> 상품: 정렬값(`latest`) 기준 정렬
            상품 ->> 상품: page, size 기준으로 페이징 (기본 5개)
            deactivate 상품
            상품 -->> Client: 200 ok `상품명`, `상품가격`, `좋아요 수` 을 반환
        end
    end
```

#### 상품 상세 정보 조회 (GET /api/v1/products/{productid})

```mermaid
sequenceDiagram
    actor Client as Client
    participant 상품 as 상품
    participant 상품.좋아요 as 상품.좋아요
    participant 상품.옵션별재고 as 상품.옵션별재고

    Client ->> 상품: 상품 상세 정보 조회 요청 (GET /api/v1/products/{productId})
    activate 상품
    상품 ->> 상품: 상품 존재 여부 및 상태 확인
    alt 조회 실패 
        alt 상품이 존재하지 않음
            상품 -->> Client: 404 Not Found
        else 상품 상태가 '판매중'이 아님
            상품 -->> Client: 400 Bad Request
        else 시스템 장애 발생
            상품 -->> Client: 500 Internal Server Error
        end
    else 조회 성공
        상품 ->> 상품.좋아요 : 해당 상품의 좋아요 수 조회
        activate 상품.좋아요
        상품.좋아요 -->> 상품 : 좋아요 수 반환
        deactivate 상품.좋아요

        상품 ->> 상품.옵션별재고 : 옵션별 재고 조회
        activate 상품.옵션별재고
        상품.옵션별재고 -->> 상품 : 옵션 및 재고 정보 반환
        deactivate 상품.옵션별재고

        상품 -->> Client: 200 ok 상세 정보 반환
        deactivate 상품
    end

```
---

## 좋아요
#### 좋아요 등록  (POST /api/v1/products/{productId}/likes )

```mermaid
sequenceDiagram
    actor Client as Client
    participant 상품 as 상품
    participant 사용자 as 사용자

    Client ->> 상품: 좋아요 등록 요청 (GET /api/v1/products/{productId}/likes)

    activate 사용자
    상품 ->> 사용자: 사용자 존재여부 확인
    alt 유효하지 않은 사용자일 경우 
        사용자 -->> Client: 404 Not Found
    else
        사용자 -->> 상품: 사용자 정보 반환
    end
    deactivate 사용자

    activate 상품
    상품 ->> 상품: 상품 존재여부 및 상태 확인
    alt 유효하지 않은 상품일 경우
        상품 -->> Client: 404 Not Found
    else
        상품 ->> 상품: 상품 정보 반환
    end
    deactivate 상품
    
    상품 ->> 상품: 좋아요 등록
    activate 상품
    alt 좋아요 등록 실패 (시스템 장애)
        상품 -->> Client: 500 Internal Server Error
    else 좋아요 등록 성공
        상품 -->> Client: 200 OK
    end
    deactivate 상품
``` 
#### 좋아요 취소  (DELETE /api/v1/products/{productId}/likes )

```mermaid
sequenceDiagram
    actor Client as Client
    participant 상품 as 상품
    participant 사용자 as 사용자

    Client ->> 상품: 좋아요 등록 요청 (GET /api/v1/products/{productId}/likes)

    activate 사용자
    상품 ->> 사용자: 사용자 존재여부 확인
    alt 유효하지 않은 사용자일 경우 
        사용자 -->> Client: 404 Not Found
    else
        사용자 -->> 상품: 사용자 정보 반환
    end
    deactivate 사용자

    activate 상품
    상품 ->> 상품: 좋아요한 상품인지 확인
    alt 좋아요한 상품이 아닌 경우 
        상품 -->> Client: 400 Bad Request
    else 좋아요 취소 성공
        상품 -->> Client: 200 OK
    end
    deactivate 상품
```

#### 내가 좋아요한 상품 목록 조회 (GET /api/v1/products/likes)
```mermaid
sequenceDiagram
    actor Client as Client
    participant 상품 as 상품
    participant 사용자 as 사용자

    Client ->> 상품: 내가 좋아요한 상품 목록 조회 요청 (GET /api/v1/products/likes)

    activate 사용자
    상품 ->> 사용자: 사용자 존재여부 확인
    alt 유효하지 않은 사용자일 경우 
        사용자 -->> Client: 404 Not Found
    else
        사용자 -->> 상품: 사용자 정보 반환
    end
    deactivate 사용자

    activate 상품
    상품 ->> 상품: 내가 좋아요한 상품 목록 조회 요청
    alt 시스템 에러
        상품 -->> Client: 500 Internal Server Error
    else 내가 좋아요한 상품 목록 조회 성공
        상품 -->> Client: 200 OK
    end
    deactivate 상품

```
---
## 주문
#### 주문요청 (POST /api/v1/orders)

```mermaid
sequenceDiagram
    actor Client as Client
    participant 주문 as 주문
    participant 상품 as 상품
    participant 상품옵션재고 as 상품.옵션별재고
    participant 사용자 as 사용자

    Client ->> 주문: 주문요청 (POST /api/v1/orders)

    activate 사용자
    주문 ->> 사용자: 사용자 조회 
    alt 유효하지 않은 사용자일 경우 
        사용자 -->> Client: 404 Not Found (사용자 없음)
    else 사용자 존재
        사용자 -->> 주문: 사용자 정보 반환
        주문 ->> 상품옵션재고: 재고 조회
        deactivate 사용자
        activate 상품옵션재고
        alt 존재하지 않는 상품 옵션
            상품옵션재고 -->> Client: 404 Not Found (상품옵션 없음)
        else 재고 부족
            상품옵션재고 -->> Client: 400 Bad Request (재고 부족)
        else 재고 충분
        
            상품옵션재고 -->> 주문: 재고 선점 완료
            deactivate 상품옵션재고
            activate 상품
            주문 ->> 상품: 전체 옵션 재고 확인
            상품 ->> 상품: 모든 옵션 재고가 0인지 확인
            alt 전체 품절 상태
                상품 ->> 상품: 상태를 `SOLD_OUT`로 변경
            end
            deactivate 상품
            activate 주문
            주문 ->> 주문: 주문 생성(주문금액, 옵션목록, 주문일자)
            주문 -->> Client: 200 OK + 주문정보 반환
            deactivate 주문
        end
    end
 ```

#### 주문취소요청  (POST /api/v1/orders/{orderId}/cancellation)
```mermaid
sequenceDiagram
    actor Client as Client
    participant 주문 as 주문
    participant 재고 as 상품옵션재고

    Client ->> 주문: 주문취소요청 (POST /api/v1/orders/{orderId}/cancellation)
    activate 주문
    주문 ->> 주문: 주문 조회
    alt 존재하지 않는 주문
        주문 -->> Client : 404 Not Found
    else 이미 취소된 주문
        주문 -->> Client : 409 Conflict
    else 본인의 주문이 아님
        주문 -->> Client :  403 Forbidden
    else 취소 가능한 주문
        주문 -->> 주문: 주문정보 반환
        activate 재고 
        주문 ->> 재고: 선점된 재고 원복
        deactivate 재고
        주문 ->> 주문: 주문 상태 `CANCELED`로 변경
        주문 -->> Client: 200 OK
    end
    deactivate 주문
```
#### 주문 목록 조회 (GET /api/v1/users/{userId}/orders)

```mermaid
sequenceDiagram
    actor Client as Client
    participant 주문 as 주문
    participant 사용자 as 사용자

    Client ->> 주문 : 주문목록조회 요청 (GET /api/v1/users/{userId}/orders)
    
    activate 사용자
    주문 ->> 사용자 : 사용자 조회
    alt 유효하지 않은 사용자일 경우 
        사용자 -->> Client: 404 Not Found (사용자 없음)
    else 사용자 존재
        사용자 -->> 주문: 사용자 정보 반환
    end
    deactivate 사용자

    activate 주문
    주문 -->> 주문 : 주문목록 조회
    alt 시스템 에러
        주문 -->> Client: 500 Internal Server Error
    else 주문목록 조회 성공
        주문 -->> Client: 200 OK + 주문 목록 응답
    end     
    deactivate 주문
```

#### 단일 주문 조회 (GET / api/v1/orders/{orderId})

```mermaid
sequenceDiagram
    actor Client as Client

    participant 주문 as 주문
    participant 상품 as 상품

    Client ->> 주문: 단일 주문 조회 GET /api/v1/orders/{orderId}

    activate 주문
    주문 ->> 주문: 주문 조회
    alt 존재하지 않는 주문 
        주문 -->> Client : 404 Not Found
    else 주문 존재
        activate 상품
        주문 ->> 상품 : 주문목록별 상품정보, 옵션정보 조회
        alt 시스템 에러
            상품 -->> Client: 500 Internal Server Error
        else 주문상세 조회 성공
            상품 -->> 주문: 주문목록별 상품정보, 옵션정보 반환
            deactivate 상품
        end 
        주문 -->> Client: 200 OK + 주문 상세 응답
    end
    deactivate 주문
```

---
## 결제
#### 결제요청  (POST /api/v1/orders/{orderId}/payments)

```mermaid
sequenceDiagram
    actor Client as Client

    participant 결제 as 결제
    participant 주문 as 주문
    participant 포인트 as 포인트
    participant 사용자 as 사용자

    Client ->> 결제: 결제요청 POST /api/v1/orders/{orderId}/payments
    activate 사용자
    결제 ->> 사용자 : 사용자 조회
    alt 유효하지 않은 사용자일 경우 
        사용자 -->> Client: 404 Not Found (사용자 없음)
    else 사용자 존재
        사용자 -->> 결제: 사용자 정보 반환
    end
    deactivate 사용자

    activate 주문
    결제 ->> 주문 : 주문 정보 조회 
    alt 존재하지 않는 주문정보 
        주문 -->> Client: 404 Not Found 
    else "결제 전" 상태의 주문
        주문 -->> Client: 409 Conflict
    else 주문정보 조회 성공
        주문 -->> 결제: 주문정보 반환
    end
    deactivate 주문

    activate 포인트
    결제 ->> 포인트 : 포인트 정보 조회 
    alt 결제금액이 포인트보다 클 경우 
        포인트 -->> Client: 400 Bad Request
    else 결제가 가능한 경우
        포인트 ->> 포인트 : 포인트 차감
    end
    deactivate 포인트

    activate 주문
    주문 ->> 주문 : 주문 상태 변경 (주문완료)
    deactivate 주문

    activate 결제
    결제 ->> 결제 : 결제정보 저장
    deactivate 결제
    결제 -->> Client : 200 Ok 결제정보 응답

``` 
#### 결제취소요청 (DELETE /api/v1/orders/{orderId}/payments/{paymentId})
 ```mermaid
 sequenceDiagram
   actor Client as Client

   participant 결제 as 결제
   participant 주문 as 주문
   participant 포인트 as 포인트
   participant 상품옵션별재고 as 상품.옵션별재고
   participant 사용자 as 사용자

   activate 결제

   Client ->> 결제: 결제취소요청 DELETE /api/v1/orders/{orderId}/payments/{paymentId}
   activate 사용자
   결제 ->> 사용자 : 사용자 조회
   alt 유효하지 않은 사용자일 경우 
       사용자 -->> Client: 404 Not Found 
   else 사용자 존재
       사용자 -->> 결제: 사용자 정보 반환
   end
   deactivate 사용자

   결제 ->> 결제 : 결제 정보 조회

   activate 주문
   결제 ->> 주문 : 주문 정보 조회 
   alt 존재하지 않는 주문정보 
       주문 -->> Client: 404 Not Found 
   else 이미 취소된 주문
       주문 -->> Client: 409 Conflict
   else 주문정보 조회 성공공
       주문 -->> 결제: 주문정보 반환
   end
   deactivate 주문

   activate 상품옵션별재고
   결제 ->> 상품옵션별재고 : 재고 정보 조회
   상품옵션별재고 ->> 상품옵션별재고 : 주문정보에 기반한 재고수량 원복
   alt 시스템 에러
       상품옵션별재고 -->> Client: 500 Internal Server Error
   end
   deactivate 상품옵션별재고

   activate 주문
   결제 ->> 주문 : 주문 정보 조회
   주문 -->> 주문: 주문상태 변경 "주문취소"
   deactivate 주문

   activate 포인트
   결제 ->> 포인트 : 포인트 복구
   포인트 -->> 포인트 : 포인트 복구
   deactivate 포인트

   결제 -->> 결제 : 결제상태 "결제 취소" 로 변경
   
   deactivate 결제
 ```
