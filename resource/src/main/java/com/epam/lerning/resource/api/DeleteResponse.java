package com.epam.lerning.resource.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


@AllArgsConstructor
@Data
public class DeleteResponse {
	private List<Long> ids;
}
