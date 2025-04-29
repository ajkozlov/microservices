package contracts.api

import org.springframework.cloud.contract.spec.Contract;

Contract.make {
    description "Should return song by id=1"

    request {
        url "/songs/1"
        method GET()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
                "name":"Name",
                "artist":"Artist",
                "album":"Album",
                "duration":"125",
                "year":"2020"
        )
    }

}