package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"math/rand"
	"net/http"
	"time"
)

func main() {
	fmt.Printf("START %s\n", time.Now())

	var uuids []string
	response, err := http.Get("http://localhost:8080/attributes")
	if err != nil {
		log.Fatal(err)
	}

	defer response.Body.Close()
	body, err := ioutil.ReadAll(response.Body)
	if err != nil {
		log.Print(err)
		return
	}
	if string(body) != "" {
		fmt.Println(string(body))
	}

	json.Unmarshal(body, &uuids)
	fmt.Println(uuids)

	lastPrintTime := time.Now()
	for i := 1; i <= 1_000_000; i++ {
		executions := 1000
		if i%executions == 0 {
			now := time.Now()
			diffSecondsLastPrint := now.Unix() - lastPrintTime.Unix()
			execPerSec := executions
			if diffSecondsLastPrint != 0 {
				execPerSec = int(int64(executions) / diffSecondsLastPrint)
			}
			fmt.Printf("%d %s (diff %d sec) => requests / sec = %d\n", i, now, diffSecondsLastPrint, execPerSec)
			lastPrintTime = now
		}

		executeOneRequestAttribute(uuids)
		time.Sleep(20 * time.Millisecond)
	}
	fmt.Printf("END %s\n", time.Now())
}

func executeOneRequestAttribute(uuids []string) {
	r := int(rand.Int31n(int32(len(uuids))))
	var attr string
	if r < len(uuids) {
		attr = uuids[r]
	} else {
		attr = uuids[0] + "abc"
	}
	executeHttpRequest("http://localhost:8080/attribute", attr, false)
}

func executeHttpRequest(endpoint string, json string, processing bool) {

	var resp *http.Response
	var err error
	if processing {
		resp, err = http.Post("http://localhost:8080/process", "application/json", bytes.NewBufferString(json))
	} else {
		resp, err = http.Get(endpoint + "?id=" + json)
	}

	if err != nil {
		log.Print(err)
		return
	}

	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		log.Print(err)
		return
	}
	if false && string(body) != "" {
		fmt.Println(string(body))
	}
}
