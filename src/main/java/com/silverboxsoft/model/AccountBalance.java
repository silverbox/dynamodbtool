package com.silverboxsoft.model;

import lombok.Data;

@Data
public class AccountBalance {

	// @JsonProperty("tgt_date")
	private String tgtDate;

	// @JsonProperty("method_cd")
	private String methodCd;

	// @JsonProperty("value")
	private Long value;

}
