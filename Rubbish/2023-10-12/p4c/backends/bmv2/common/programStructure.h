/*
Copyright 2013-present Barefoot Networks, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

#ifndef BACKENDS_BMV2_COMMON_PROGRAMSTRUCTURE_H_
#define BACKENDS_BMV2_COMMON_PROGRAMSTRUCTURE_H_

#include "ir/visitor.h"
#include "lib/ordered_set.h"
#include "metermap.h"

namespace BMV2 {

using ResourceMap = ordered_map<const IR::Node *, const IR::CompileTimeValue *>;

enum class BlockConverted {
    None,
    Parser,
    Ingress,
    Egress,
    Deparser,
    ChecksumCompute,
    ChecksumVerify
};

// Represents all the compile-time information about a P4-16 program that
// is common to all bmv2 targets (simple switch and psa switch).
class ProgramStructure {
 public:
    /// Map action to parent control.
    ordered_map<const IR::P4Action *, const IR::P4Control *> actions;
    /// Maps each Parameter of an action to its positional index.
    /// Needed to generate code for actions.
    ordered_map<const IR::Parameter *, unsigned> index;
    /// Parameters of controls/parsers
    ordered_set<const IR::Parameter *> nonActionParameters;
    /// For each action its json id.
    ordered_map<const IR::P4Action *, unsigned> ids;
    /// All local variables.
    std::vector<const IR::Declaration_Variable *> variables;
    /// All error codes.
    ordered_map<const IR::IDeclaration *, unsigned int> errorCodesMap;
    // We place scalar user metadata fields (i.e., bit<>, bool)
    // in the scalarsName metadata object, so we may need to rename
    // these fields.  This map holds the new names.
    std::map<const IR::StructField *, cstring> scalarMetadataFields;
    /// All the direct meters.
    DirectMeterMap directMeterMap;
    /// All the direct counters.
    ordered_map<cstring, const IR::P4Table *> directCounterMap;
    /// All match kinds
    std::set<cstring> match_kinds;
    /// map IR node to compile-time allocated resource blocks.
    ResourceMap resourceMap;

    ProgramStructure() {}
};

class DiscoverStructure : public Inspector {
 public:
    ProgramStructure *structure;

    explicit DiscoverStructure(ProgramStructure *structure) : structure(structure) {
        setName("DiscoverStructure");
    }
    void postorder(const IR::ParameterList *paramList) override;
    void postorder(const IR::P4Action *action) override;
    void postorder(const IR::Declaration_Variable *decl) override;
    void postorder(const IR::Type_Error *errors) override;
    void postorder(const IR::Declaration_MatchKind *kind) override;
};

// The resource map represents the mapping from IR::Node to IR::Block. This
// pass relies on the information generated by the most recent Evaluator pass.
// The Evaluator pass generates a mapping from IR::Block to IR::Node. This pass
// provides a reversed map.
class BuildResourceMap : public Inspector {
    ResourceMap *resourceMap;

 public:
    explicit BuildResourceMap(ResourceMap *resourceMap) : resourceMap(resourceMap) {
        CHECK_NULL(resourceMap);
    }

    bool preorder(const IR::ControlBlock *control) override {
        resourceMap->emplace(control->container, control);
        for (auto cv : control->constantValue) {
            resourceMap->emplace(cv.first, cv.second);
        }

        for (auto c : control->container->controlLocals) {
            if (c->is<IR::InstantiatedBlock>()) {
                resourceMap->emplace(c, control->getValue(c));
            }
        }
        return false;
    }

    bool preorder(const IR::ParserBlock *parser) override {
        resourceMap->emplace(parser->container, parser);
        for (auto cv : parser->constantValue) {
            resourceMap->emplace(cv.first, cv.second);
            if (cv.second->is<IR::Block>()) {
                visit(cv.second->getNode());
            }
        }

        for (auto c : parser->container->parserLocals) {
            if (c->is<IR::InstantiatedBlock>()) {
                resourceMap->emplace(c, parser->getValue(c));
            }
        }
        return false;
    }

    bool preorder(const IR::TableBlock *table) override {
        resourceMap->emplace(table->container, table);
        for (auto cv : table->constantValue) {
            resourceMap->emplace(cv.first, cv.second);
            if (cv.second->is<IR::Block>()) {
                visit(cv.second->getNode());
            }
        }
        return false;
    }

    bool preorder(const IR::PackageBlock *package) override {
        for (auto cv : package->constantValue) {
            if (cv.second->is<IR::Block>()) {
                visit(cv.second->getNode());
            }
        }
        return false;
    }

    bool preorder(const IR::ToplevelBlock *tlb) override {
        auto package = tlb->getMain();
        visit(package);
        return false;
    }
};

}  // namespace BMV2

#endif /* BACKENDS_BMV2_COMMON_PROGRAMSTRUCTURE_H_ */
