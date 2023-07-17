# spring-batch-practice

## Job

![spring-batch-job](./image/spring-batch-job.png)

![jobinstance-jobexecution.png](./image/jobinstance-jobexecution.png)


### JobLauncher

![spring-batch-job-component-relation.png](./image/spring-batch-job-component-relation.png)

- 잡을 실행하는 역할
- 잡의 재실행 가능 여부 검증
- 잡의 실행 방법
- 파라미터 유효성 검증

> `@EnableBatchProcessing`  
> 스프링 부트 3 버전 이상부터는 더이상 필요하지 않음  
> 추가하게 되면 기존의 자동 구성이 사라지게 됨

#### JobRunner

- 잡 이름과 여러 파라미터를 받아 잡을 실행시키는 역할
- 프레임워크가 제공하는 표준 모듈이 아님
- 실제 진입점은 잡 러너가 아닌 `JobLanuncher` 의 구현체
- 두 개의 잡 러너가 존재
  - `CommandLineJobRunner` (스프링 부트에서는 `JobJauncherCommandLineRunner` 도 제공)
  - `JobRegistryBackgroundJobRunner`


### JobRepository

- 청크의 처리가 스텝 내에서 완료되면 JobRepository 내부에 있는 JobExecution 또는 StepExecution 에 상태를 갱신
- 청크를 처리할 때마다 커밋 수, 시작 및 종료 시간, 기타 정보가 있는 StepExecution 의 스텝 상태 업데이트

### JobInstance

- 스프링 배치 잡의 논리적인 실행, 잡의 이름과 잡의 논리적인 실행을 위해 제공되는 고유한 식별 파라미터 모음
- 잡이 실행될 때마다 새로운 JobInstance 생성
- 실패한 잡을 재식작하면 JobInstance 은 생성되지 않음

### JobExecution

- 스프링 배치 잡의 실제 실행
- 잡을 구동할 때마다 새로운 JobExecution 생성
- 잡이 실패한 이후 다시 실행해도 새로운 JobExecution 생성

### StepExecution

- 스텝의 실제 실행
- JobExecution 은 StepExecution 여러 개와 연관

### 구성 요소

- `JobRepository`
  - 실행 중인 잡의 상태를 기록하는 데 사용
- `JobLauncher`
  - 잡을 구동하는 데 사용
- `JobExplorer`
  - JobRepository 을 사용해 읽기 전용 작업을 수행하는데 사용
- `JobRegistry`
  - 특정 런처 구현체를 사용할 때 잡을 찾는 용도
- `PlatformTransactionManager`
  - 잡 진행 과정에서 트랜잭션을 다루는데 사용
- `JobBuilderFactory`
  - 잡을 생성하는 빌더 (`JobBuilder` 로 대체)
- `StepBuilderFactory`
  - 스텝을 생성하는 빌더 (`StepBuilder` 로 대체)

- `JobParameters`
  - `Map<String, JobParameter>` 객체의 래퍼
  - 타입 이름은 모두 **소문자**여야 함
  - `JobLauncherCommandLineRunner` 에 파라미터를 전달하기 위해서는 명령행으로 `key=value` 쌍을 전달하면 됨
    - ex) `java -jar demo.jar name=test`
    - 타입 변환 기능을 사용하고 싶으면 파라미터 이름 뒤에 **괄호를 쓰고 타입을 명시**하면 됨
      - ex) `java -jar demo.jar executionDate(date)=2020/01/01`
    - 잡 파라미터가 식별에 사용되지 않으려면 **접두사** `-` 추가
      - ex) `java -jar demo.jar executionDate(date)=2020/01/01 -name=foo`
    - 명령행 기능을 사용해 프로퍼티 구성하는 것과 다르므로 `--`, `-D` 접두사를 사용하면 안됨
    - intellij 에서 잡 파라미터를 전달하고 싶다면 `program arguments` 를 이용하면 됨

- `JobParametersValidator`
  - 잡 파라미터의 유효성 검증
  - 기본적으로 필수 파라미터가 전달됐는지 확인하는 `DefaultJobParametersValidator` 제공
  - 여러 개의 검증기를 사용하기 위해서는 `CompositeJobParameterValidator` 제공

- `JobParametersIncrementer`
  - 잡에서 사용할 파라미터를 고유하게 생성할 수 있도록 배치에서 제공하는 인터페이스
  - `RunIdIncrementer` 을 사용하면 `run.id` 인 `long` 타입 파라미터의 값을 증가
  - 실행 시마다 타임스탬프를 파라미터로 사용하고 싶다면 직접 구현 필요

- `JobExecutionListener`
  - 스프링 배치 생명주기의 여러 시점에 로직을 추가할 수 있도록 도와주는 인터페이스
  - 스텝, 리더, 라이터 등 컴포넌트에도 사용 가능
  - `JobExecutionListener` 을 구현하지 않아도 `@BeforeJob`, `@AfterJob` 애노테이션 사용 가능
    - `JobListenerFactoryBean.getListner` 를 통해 `JobExecutionListener` 를 구현한 빈을 등록
  - `beforeJob`, `afterJob` 두 메서드 제공
  - 사용 사례
    - 알림: 잡의 시작이나 종료를 알리는 메세지 큐 생성
    - 초기화: 잡 실행 전에 뭔가 준비해야 한다면 `beforeJob` 에서 수행
    - 정리: 잡의 성공/실패와 관계없이 실행 후에 뭔가 정리해야 한다면 `afterJob` 에서 수행

- `ExecutionContext`
  - 배치 잡의 세션 (간단하게 키-값 쌍을 보관하는 도구)
  - `JobExecution`, `StepExecution` 의 일부로 잡의 상태를 저장하는 곳
  - 잡을 다루는 과정에서 여러 개의 `ExecutionContext` 이 존재할 수 있음
  - 스텝 간에 데이터를 공유하고 싶다면 `ExecutionContextPromotionlistner` 사용


## 스텝

잡을 구성하는 독립된 작업의 단위  
태스크릿(tasklet)과 청크(chunk) 기반의 두 가지 스텝 유형 존재

- 태스크릿(tasklet)
    - `Tasklet.execute` 메서드가 `RepeatStatus.FINISHED` 를 반환할 때까지 트랜잭션 범위 내에서 반복 실행
    - 초기화, 저장 프로시저 실행, 알림 전송등에 많이 사용
    - 구현 방법에는 두 가지 유형이 존재
      - `Tasklet` 인터페이스의 `execute` 메서드 구현하여 `RepeatStatus` 반환하여 정의 가능
        - `RepeatStatus.CONTINUABLE`: 잡을 계속 수행하는 것이 아닌 해당 태스크릿을 다시 실행
        - `RepeatStatus.FINISHED`: 처리의 성공 여부에 관계 없이 처리를 완료하고 다음처리를 이어서 함
      - 다른 유형의 태스크릿
        - `CallableTaskletAdapter`: `java.util.concurrent.Callable<RepeatStatus>` 의 구현체를 구성할 수 있게 해주는 어댑터
        - `MethodInvokingTaskletAdapter`: 다른 클래스 내의 메서드를 잡 내의 태스크릿처럼 실행
        - `SystemCommandTasklet`: 시스템 명령을 비동기로 실행

- 청크(chunk)
  - 아이템 기반의 처리에 사용
  - 커밋 간격(commit interval)에 의해 정의
  - 3개의 주요 부분으로 구성될 수 있음
    - `ItemReader` : 데이터를 읽어옴
      - 첫번째 루프로 청크 단위로 처리할 모든 레코드를 반복적으로 메모리로 가져옴
    - `ItemProcessor` : 데이터를 가공(선택)
      - 메모리로 읽어온 아이템들을 반복적으로 수행
    - `ItemWriter` : 데이터를 저장
      - 물리적 쓰기를 일괄적으로 처리
  - `CompletionPolicy` 를 통해 청크가 완료되는 시점 정의
    - `SimpleCompletionPolicy`: 처리된 아이템 개수를 세고 구성해둔 임계 값에 도달하면 완료 표시
    - `TimeoutTerminationPolicy`: 처리 시간이 해당 시간을 넘길 때 완료된 것으로 간주
    - `CompositeCompletionPolicy`: 여러 정책을 함께 구성 가능
  ![chunk-sequence-diagram.png](./image/chunk-sequence-diagram.png)

